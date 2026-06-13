package sophena.calc.energy;

import java.util.Objects;

import sophena.model.HeatNet;
import sophena.model.HoursTrace;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.SolarCollector;
import sophena.model.SolarCollectorSpec;
import sophena.model.Stats;
import sophena.utils.Temperature;

class SolarState {

	private final SolarLog log;
	private final Project project;
	private final Producer producer;
	private final SolarCollectorSpec spec;
	private final SolarCollector collector;

	private SolarPhase phase;
	private OperationMode operationMode;
	int numStagnationDays;
	double TK_i;

	private double TK_i_minus_one;
	private double A;
	private double C;
	private double QS_i_before_correction;
	private double meanCollectorTemperature;
	private double QS_i;


	SolarState(SolarLog log, Project project, Producer producer) {
		this.log = log;
		this.project = project;
		this.producer = producer;
		this.spec = Objects.requireNonNull(producer.solarCollectorSpec);
		this.collector = Objects.requireNonNull(producer.solarCollector);

		phase = SolarPhase.WARM_UP;
		operationMode = OperationMode.PRE_HEATING;
		TK_i_minus_one = Temperature.of(project, 0);
		numStagnationDays = 0;
	}

	boolean isNotOperating() {
		return phase != SolarPhase.OPERATION;
	}

	void calcPre(int hour, double TE, double TV) {

		log.beginProducer(producer);

		// Input

		// 1
		double ALB = 0.2;

		// m²
		A = spec.solarCollectorArea;
		// 1
		double ETA0 = collector.efficiencyRateRadiation;
		// 1
		double KDF = collector.correctionFactor;
		// W/m²/K
		double A1 = collector.heatTransferCoefficient1;
		// W/m²/K²
		double A2 = collector.heatTransferCoefficient2;
		// Wh/m²/K
		C = collector.heatCapacity;
		// -180°..+180°
		double NW = spec.solarCollectorTilt;
		// 0°..+90°
		double AUS = spec.solarCollectorAlignment;
		// K
		double UEH = spec.solarCollectorTemperatureIncrease;
		// K
		double TD = spec.solarCollectorTemperatureDifference;

		var station = project.weatherStation;

		// °
		double BG = station.latitude;

		// °
		double LG = -station.longitude;

		// °
		double MERI = -station.referenceLongitude;

		// °C
		double TL_i = Temperature.of(project, hour);

		// W/m²
		double SDI_i = Stats.get(station.directRadiation, hour);

		// W/m²
		double SDF_i = Stats.get(station.diffuseRadiation, hour);

		// 1..365
		int JT = 1 + hour / 24;
		// 1..24
		int TS = 1 + hour % 24;

		// Calculation

		// rad
		double B = (JT - 1) / 365.0 * 2 * Math.PI;
		// min
		double E = 229.2 * (0.000075 + 0.001868 * Math.cos(B) - 0.032077 * Math.sin(B) - 0.014615 * Math.cos(2 * B) - 0.04089 * Math.sin(2 * B));
		// h
		double SZ = ((TS - 0.5) * 3600 + E * 60 + 4 * (MERI - LG) * 60) / 3600;
		// °
		double SD = 23.45 * Math.sin((284 + JT) / 365.0 * 2 * Math.PI);
		// °
		double HW = -180 + SZ * 180 / 12;

		// rad
		double SZW = Math.acos(Math.cos(Math.toRadians(BG)) * Math.cos(Math.toRadians(HW)) * Math.cos(Math.toRadians(SD)) + Math.sin(Math.toRadians(BG)) * Math.sin(Math.toRadians(SD)));
		// rad
		double SAW = Math.signum(HW) * Math.acos(
			(Math.cos(SZW) * Math.sin(Math.toRadians(BG)) - Math.sin(Math.toRadians(SD))) / (Math.sin(SZW) * Math.cos(Math.toRadians(BG)))
		);
		// rad
		double EWK = Math.acos(
			Math.cos(SZW) * Math.cos(Math.toRadians(NW)) + Math.sin(SZW) * Math.sin(Math.toRadians(NW)) * Math.cos(SAW - Math.toRadians(AUS)) + 0.0000000001
		);

		// rad
		double EWOW = Math.toDegrees(SZW) < 90 && Math.toDegrees(EWK) < 90
			? Math.atan(Math.sin(SZW) * Math.sin(SAW - Math.toRadians(AUS)) / Math.cos(EWK))
			: Math.toRadians(89.999);

		// rad
		double EWNS = Math.toDegrees(SZW) < 90 && Math.toDegrees(EWK) < 90
			? -(Math.atan(Math.tan(SZW) * Math.cos(SAW - Math.toRadians(AUS))) - Math.toRadians(NW))
			: Math.toRadians(89.990);

		// 1
		double EWFOW = getEWFOW(Math.toDegrees(EWOW));
		// 1
		double EWFNS = getEWFNS(Math.toDegrees(EWNS));

		// 1
		double KOF = Math.toDegrees(SZW) < 90 && Math.toDegrees(EWK) < 90
			? Math.cos(EWK) / Math.cos(SZW)
			: 0;

		// 1 (1367 is solar constant in W/m²)
		double RAB = SDI_i / (1367.0 * (1.0 + 0.033 * Math.cos(360 * JT / 365.0 * Math.PI / 180)) * Math.cos(SZW));

		// W/m²
		double SGK = SDI_i * KOF + SDF_i * RAB * KOF + SDF_i * (1 - RAB) * 0.5 * (1 + Math.cos(Math.toRadians(NW))) + (SDI_i + SDF_i) * ALB * (1 - 0.5) * (1 - Math.cos(Math.toRadians(NW)));

		// W/m²
		double SDIK = SDI_i * KOF;

		// W/m²
		double SDFK = SGK - SDIK;

		// 1
		double EWF = EWFOW * EWFNS;

		double radiation = ETA0 * EWF * SDIK + ETA0 * KDF * SDFK;

		// Wh
		QS_i = (radiation - A1 * (TK_i_minus_one - TL_i) - A2 * sqr(TK_i_minus_one - TL_i)) * A * 1;

		operationMode = operationMode(project.heatNet, hour, radiation); //TODO

		double inletTemperature;
		double outletTemperature;

		switch (operationMode) {
			case PRE_HEATING:
				inletTemperature = TE + UEH;
				outletTemperature = TE + UEH + TD;
				break;
			case TARGET_TEMPERATURE:
				inletTemperature = TE + UEH;
				if (TV >= TE)
					outletTemperature = TV + UEH;
				else
					outletTemperature = TE + UEH + TD;
				break;
			default:
				inletTemperature = 0;
				outletTemperature = 0;
				break;
		}

		meanCollectorTemperature = (inletTemperature + outletTemperature) * 0.5;
		if (meanCollectorTemperature > project.heatNet.maxBufferLoadTemperature && phase != SolarPhase.STAGNATION) {
			operationMode = OperationMode.HIGH_TEMPERATURE;
			log.message("Changing Operation Mode to " + operationMode);
		}

		QS_i_before_correction = QS_i;

		if (TS == 1) {
			log.beginDay(JT, hour,
				"Hour",
				"ALB",
				"A",
				"ETA0",
				"KDF",
				"A1 [W/m²/K]",
				"A2 [W/m²/K²]",
				"C [Wh/m²/K]",
				"NW [°]",
				"AUS [°]",
				"UEH [K]",
				"TD [K]",
				"BG [°]",
				"LG [°]",
				"MERI [°]",
				"TL_i [°C]",
				"SDI_i [W/m²]",
				"SDF_i [W/m²]",
				"JT",
				"TS",
				"B [rad]",
				"E [min]",
				"SZ [h]",
				"SD [°]",
				"HW [°]",
				"SZW [rad]",
				"SAW [rad]",
				"EWK [rad]",
				"EWOW [rad]",
				"EWNS [rad]",
				"EWFOW",
				"EWFNS",
				"KOF",
				"RAB",
				"SGK [W/m²]",
				"SDIK [W/m²]",
				"SDFK [W/m²]",
				"EWF",
				"radiation",
				"BA_i",
				"PHASE_i",
				"TS",
				"TK_i-1 [°C]",
				"QS_i_uncorr [Wh]",
				"QS_i [Wh]",
				"TK_i [°C]",
				"consumedPower",
				"kollmitteltemp",
				"Load type"
			);

			phase = SolarPhase.WARM_UP;
			log.message("Changing Phase to " + phase);

			TK_i_minus_one = TL_i;
		}

		switch (phase) {
			case STAGNATION:
				QS_i = 0;
				if (TS == 24)
					numStagnationDays++;
				break;
			case WARM_UP: {
				double temperatur = TK_i_minus_one + QS_i / (A * C);

				if (temperatur > meanCollectorTemperature) {
					phase = SolarPhase.OPERATION;
					log.message("Changing Phase to " + phase);

					TK_i_minus_one = meanCollectorTemperature;
				} else {
					TK_i = Math.max(temperatur, TL_i);
				}
				QS_i = 0;
			}
			break;
			case OPERATION: {
				double temperatur = TK_i_minus_one + QS_i / (A * C);

				if (temperatur > meanCollectorTemperature) {
					QS_i = (temperatur - meanCollectorTemperature) * A * C;
					TK_i = meanCollectorTemperature;
				} else {
					phase = SolarPhase.WARM_UP;
					log.message("Changing Phase to " + phase);

					TK_i = Math.max(temperatur, TL_i);
					QS_i = 0;
				}
			}
			break;
		}

		log.hourValues(hour,
			false,
			hour,
			ALB,
			A,
			ETA0,
			KDF,
			A1,
			A2,
			C,
			NW,
			AUS,
			UEH,
			TD,
			BG,
			-LG,
			-MERI,
			TL_i,
			SDI_i,
			SDF_i,
			JT,
			TS,
			B,
			E,
			SZ,
			SD,
			HW,
			SZW,
			SAW,
			EWK,
			EWOW,
			EWNS,
			EWFOW,
			EWFNS,
			KOF,
			RAB,
			SGK,
			SDIK,
			SDFK,
			EWF,
			radiation
		);

		consumedPower = 0;
	}

	private double consumedPower;

	void setConsumedPower(double consumedPower) {
		this.consumedPower = producer.utilisationRate == null || producer.utilisationRate == 0
			? 0
			: consumedPower / producer.utilisationRate;
	}

	public void calcPost(int hour) {
		boolean writeLog = false;
		log.beginProducer(producer);

		if (phase == SolarPhase.OPERATION) {
			double deltaQS = QS_i - consumedPower;
			TK_i = TK_i_minus_one + deltaQS / (A * C);

			if (TK_i > project.heatNet.maxBufferLoadTemperature && deltaQS > 0) {
				phase = SolarPhase.STAGNATION;
				writeLog = true;
			}
		}

		int TS = 1 + hour % 24;

		log.hourValues(hour,
			true,
			operationMode,
			phase,
			TS,
			TK_i_minus_one,
			QS_i_before_correction,
			QS_i,
			TK_i,
			consumedPower,
			meanCollectorTemperature,
			getBufferLoadType()
		);

		if (writeLog)
			log.message("Changing Phase to " + phase);

		TK_i_minus_one = TK_i;
	}

	BufferLoadType getBufferLoadType() {
		return switch (operationMode) {
			case TARGET_TEMPERATURE -> BufferLoadType.FLOW_TEMP;
			case HIGH_TEMPERATURE -> BufferLoadType.HIGH_TEMP;
			case PRE_HEATING -> BufferLoadType.LOW_TEMP;
		};
	}

	private OperationMode operationMode(
		HeatNet heatNet, int hour, double radiationPerSquareMeter
	) {
		if (heatNet == null || heatNet.bufferTank == null)
			return OperationMode.TARGET_TEMPERATURE;

		switch (spec.solarCollectorOperatingMode) {
			case AUTO_RADIATION:
				return radiationPerSquareMeter > spec.solarCollectorRadiationLimit
					? OperationMode.TARGET_TEMPERATURE
					: OperationMode.PRE_HEATING;
			case AUTO_SEASON:
				int[] interval = HoursTrace.getHourInterval(heatNet.intervalSummer);
				if (isHourInInterval(hour, interval))
					return OperationMode.TARGET_TEMPERATURE;
				else
					return OperationMode.PRE_HEATING;
			case PREHEATING_MODE:
				return OperationMode.PRE_HEATING;
			case TARGET_TEMPERATURE_OPERATION:
				return OperationMode.TARGET_TEMPERATURE;
			default:
				throw new UnsupportedOperationException();
		}
	}

	private static double sqr(double x) {
		return x * x;
	}

	/// Get the incidence angle modifier for east-west.
	private double getEWFOW(double degrees) {
		// Valid range in UI is -180 to +180
		int i = (int) Math.abs(degrees) / 10;
		double t = (Math.abs(degrees) % 10) / 10.0;
		return switch (i) {
			case 0 -> lerp(1, collector.angleIncidenceEW10, t);
			case 1 ->
				lerp(collector.angleIncidenceEW10, collector.angleIncidenceEW20, t);
			case 2 ->
				lerp(collector.angleIncidenceEW20, collector.angleIncidenceEW30, t);
			case 3 ->
				lerp(collector.angleIncidenceEW30, collector.angleIncidenceEW40, t);
			case 4 ->
				lerp(collector.angleIncidenceEW40, collector.angleIncidenceEW50, t);
			case 5 ->
				lerp(collector.angleIncidenceEW50, collector.angleIncidenceEW60, t);
			case 6 ->
				lerp(collector.angleIncidenceEW60, collector.angleIncidenceEW70, t);
			case 7 ->
				lerp(collector.angleIncidenceEW70, collector.angleIncidenceEW80, t);
			case 8 ->
				lerp(collector.angleIncidenceEW80, collector.angleIncidenceEW90, t);
			default -> 0;
		};
	}

	/// Get the incidence angle modifier for north-south.
	private double getEWFNS(double degrees) {
		// Valid range in UI is 0 to +90
		int i = (int) Math.abs(degrees) / 10;
		double t = (Math.abs(degrees) % 10) / 10.0;
		return switch (i) {
			case 0 -> lerp(1, collector.angleIncidenceNS10, t);
			case 1 ->
				lerp(collector.angleIncidenceNS10, collector.angleIncidenceNS20, t);
			case 2 ->
				lerp(collector.angleIncidenceNS20, collector.angleIncidenceNS30, t);
			case 3 ->
				lerp(collector.angleIncidenceNS30, collector.angleIncidenceNS40, t);
			case 4 ->
				lerp(collector.angleIncidenceNS40, collector.angleIncidenceNS50, t);
			case 5 ->
				lerp(collector.angleIncidenceNS50, collector.angleIncidenceNS60, t);
			case 6 ->
				lerp(collector.angleIncidenceNS60, collector.angleIncidenceNS70, t);
			case 7 ->
				lerp(collector.angleIncidenceNS70, collector.angleIncidenceNS80, t);
			case 8 ->
				lerp(collector.angleIncidenceNS80, collector.angleIncidenceNS90, t);
			default -> 0;
		};
	}

	/// Linear interpolation between two angles.
	private static double lerp(double a, double b, double t) {
		return (1 - t) * a + t * b;
	}

	private boolean isHourInInterval(int hour, int[] interval) {
		return hour >= interval[0] && hour <= interval[1];
	}

	double getAvailablePowerInKWh() {
		return producer.utilisationRate == null || producer.utilisationRate == 0
			? 0
			: QS_i / 1000 * producer.utilisationRate;
	}

	private enum OperationMode {
		PRE_HEATING,
		TARGET_TEMPERATURE,
		HIGH_TEMPERATURE
	}
}
