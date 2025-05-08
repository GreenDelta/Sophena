package sophena.calc;

import sophena.model.HeatNet;
import sophena.model.Project;
import sophena.model.HoursTrace;
import sophena.model.Producer;

public class SolarCalcState {
	private SolarCalcLog log;
	private Project project;
	private Producer producer;
	private SolarCalcPhase phase;
	private SolarCalcOperationMode operationMode;

	private double TK_i_minus_one = 0;
	private double TK_i;
	private double A;
	private double C;
	private double QS_i_before_correction;
	private double kollektormitteltemperatur;
	private double QS_i;

	private int numStagnationDays;

	public SolarCalcState(SolarCalcLog log, Project project, Producer producer)
	{
		this.log = log;
		this.project = project;
		this.producer = producer;
		phase = SolarCalcPhase.Aufheiz;
		operationMode = SolarCalcOperationMode.PreHeating;

		TK_i_minus_one = project.weatherStation.data[0];

		numStagnationDays = 0;
	}
	
	public void calcPre(int hour, double TE, double TV)
	{
		if(producer.solarCollector == null || producer.solarCollectorSpec == null)
			return;
		
		
		log.beginProducer(producer);

		// Input
		
		// 1
		double ALB = 0.2;

		// m²
		A = producer.solarCollectorSpec.solarCollectorArea;
		// 1
		double ETA0 = producer.solarCollector.efficiencyRateRadiation;
		// 1
		double KDF = producer.solarCollector.correctionFactor;
		// W/m²/K
		double A1 = producer.solarCollector.heatTransferCoefficient1;
		// W/m²/K²
		double A2 = producer.solarCollector.heatTransferCoefficient2;
		// Wh/m²/K
		C = producer.solarCollector.heatCapacity;
		// -180°..+180°
		double NW = producer.solarCollectorSpec.solarCollectorTilt;
		// 0°..+90°
		double AUS = producer.solarCollectorSpec.solarCollectorAlignment;
		// K
		double UEH = producer.solarCollectorSpec.solarCollectorTemperatureIncrease;
		// K
		double TD = producer.solarCollectorSpec.solarCollectorTemperatureDifference;

		// °
		double BG = project.weatherStation.latitude;

		// °
		double LG = -project.weatherStation.longitude;

		// °
		double MERI = -project.weatherStation.referenceLongitude;
		
		// °C
		double TL_i = project.weatherStation.data != null && hour < project.weatherStation.data.length
				? project.weatherStation.data[hour]
				: 0;
		// W/m²
		double SDI_i = project.weatherStation.directRadiation != null && hour < project.weatherStation.directRadiation.length 
				? project.weatherStation.directRadiation[hour]
				: 0;
		// W/m²
		double SDF_i = project.weatherStation.diffuseRadiation != null && hour < project.weatherStation.diffuseRadiation.length
				? project.weatherStation.diffuseRadiation[hour]
				: 0;

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
			(Math.cos(SZW) * Math.sin(Math.toRadians(BG)) - Math.sin(Math.toRadians(SD))) / ( Math.sin(SZW) * Math.cos(Math.toRadians(BG)))
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
		
		operationMode = CalcOperationMode(project.heatNet, hour, radiation); //TODO

		double eintrittstemperatur;
		double austrittstemperatur;

		switch(operationMode)
		{
		case PreHeating:
			eintrittstemperatur = TE + UEH;
			austrittstemperatur = TE + UEH + TD;
			break;
		case TargetTemperature:
			eintrittstemperatur = TE + UEH;
			if(TV >= TE)
				austrittstemperatur = TV + UEH;
			else
				austrittstemperatur = TE + UEH + TD;				
			break;
		default:
			eintrittstemperatur = 0;
			austrittstemperatur = 0;
			break;
		}

		kollektormitteltemperatur = (eintrittstemperatur+austrittstemperatur)*0.5;
		if(kollektormitteltemperatur > project.heatNet.maxBufferLoadTemperature && phase != SolarCalcPhase.Stagnation)
		{
			operationMode  = SolarCalcOperationMode.HighTemperature;
			log.message("Changing Operation Mode to " + operationMode);
		}

		QS_i_before_correction = QS_i;

		if(TS == 1)
		{
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

			phase = SolarCalcPhase.Aufheiz;
			log.message("Changing Phase to "+phase);
			
			TK_i_minus_one = TL_i;
		}

		switch(phase)
		{
		case Stagnation:
			QS_i = 0;
			if(TS == 24)
				numStagnationDays++;
			break;
		case Aufheiz:
			{
				double temperatur = TK_i_minus_one + QS_i / (A * C);
				
				if(temperatur > kollektormitteltemperatur)
				{
					phase = SolarCalcPhase.Betrieb;
					log.message("Changing Phase to "+phase);

					TK_i_minus_one = kollektormitteltemperatur;
				}
				else
				{
					TK_i = Math.max(temperatur, TL_i);
				}
				QS_i = 0;
			}
			break;
		case Betrieb:
			{
				double temperatur = TK_i_minus_one + QS_i / (A * C);

				if(temperatur > kollektormitteltemperatur)
				{
					QS_i = (temperatur - kollektormitteltemperatur) * A * C;
					TK_i = kollektormitteltemperatur;
				}
				else
				{
					phase = SolarCalcPhase.Aufheiz;
					log.message("Changing Phase to "+phase);
	
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

	public void setConsumedPower(double consumedPower)
	{
		this.consumedPower = producer.utilisationRate == null || producer.utilisationRate == 0
			? 0
			: consumedPower / producer.utilisationRate;
	}

	public void calcPost(int hour)
	{
		boolean writeLog = false;
		log.beginProducer(producer);

		if(phase == SolarCalcPhase.Betrieb)
		{
			double deltaQS = QS_i - consumedPower;
			TK_i = TK_i_minus_one + deltaQS / (A * C);
			
			if(TK_i > project.heatNet.maxBufferLoadTemperature && deltaQS > 0)
			{
				phase = SolarCalcPhase.Stagnation;
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
			kollektormitteltemperatur,
			getBufferLoadType()
		);

		if(writeLog)
			log.message("Changing Phase to "+phase);
		
		TK_i_minus_one = TK_i;
	}

	public BufferCalcLoadType getBufferLoadType()
	{
		switch(operationMode)
		{
			case TargetTemperature:
				return BufferCalcLoadType.VT;
			case HighTemperature:
				return BufferCalcLoadType.HT;
			case PreHeating:
				return BufferCalcLoadType.NT;
			default:
				throw new UnsupportedOperationException();
		}
	}

	private SolarCalcOperationMode CalcOperationMode(HeatNet heatNet, int hour, double radiationPerSquareMeter)
	{
		if (heatNet != null && heatNet.bufferTank == null)
			return SolarCalcOperationMode.TargetTemperature;;
		
		switch(producer.solarCollectorSpec.solarCollectorOperatingMode)
		{
		case AUTO_RADIATION:
			return radiationPerSquareMeter > producer.solarCollectorSpec.solarCollectorRadiationLimit
				? SolarCalcOperationMode.TargetTemperature
				: SolarCalcOperationMode.PreHeating;
		case AUTO_SEASON:
			int[] interval = HoursTrace.getHourInterval(heatNet.intervalSummer);
			if(isHourInInterval(hour, interval))
				return SolarCalcOperationMode.TargetTemperature;
			else
				return SolarCalcOperationMode.PreHeating;
		case PREHEATING_MODE:
			return SolarCalcOperationMode.PreHeating;
		case TARGET_TEMPERATURE_OPERATION:
			return SolarCalcOperationMode.TargetTemperature;
		default:
			throw new UnsupportedOperationException();
		}
	}
	
	private static double sqr(double x)
	{
		return x * x;
	} 
	
	private double getEWFOW(double degrees)
	{
		// Valid range in UI is -180 to +180
		int i = (int)Math.abs(degrees) / 10;
		double t = (Math.abs(degrees) % 10) / 10.0;
		switch(i)
		{
		case 0:
			return lerp(1, producer.solarCollector.angleIncidenceEW10, t);
		case 1:
			return lerp(producer.solarCollector.angleIncidenceEW10, producer.solarCollector.angleIncidenceEW20, t);
		case 2:
			return lerp(producer.solarCollector.angleIncidenceEW20, producer.solarCollector.angleIncidenceEW30, t);
		case 3:
			return lerp(producer.solarCollector.angleIncidenceEW30, producer.solarCollector.angleIncidenceEW40, t);
		case 4:
			return lerp(producer.solarCollector.angleIncidenceEW40, producer.solarCollector.angleIncidenceEW50, t);
		case 5:
			return lerp(producer.solarCollector.angleIncidenceEW50, producer.solarCollector.angleIncidenceEW60, t);
		case 6:
			return lerp(producer.solarCollector.angleIncidenceEW60, producer.solarCollector.angleIncidenceEW70, t);
		case 7:
			return lerp(producer.solarCollector.angleIncidenceEW70, producer.solarCollector.angleIncidenceEW80, t);
		case 8:
			return lerp(producer.solarCollector.angleIncidenceEW80, producer.solarCollector.angleIncidenceEW90, t);
		default:
			return 0;
		}
	}
	
	private double getEWFNS(double degrees)
	{
		// Valid range in UI is 0 to +90
		int i = (int)Math.abs(degrees) / 10;
		double t = (Math.abs(degrees) % 10) / 10.0;
		switch(i)
		{
		case 0:
			return lerp(1, producer.solarCollector.angleIncidenceNS10, t);
		case 1:
			return lerp(producer.solarCollector.angleIncidenceNS10, producer.solarCollector.angleIncidenceNS20, t);
		case 2:
			return lerp(producer.solarCollector.angleIncidenceNS20, producer.solarCollector.angleIncidenceNS30, t);
		case 3:
			return lerp(producer.solarCollector.angleIncidenceNS30, producer.solarCollector.angleIncidenceNS40, t);
		case 4:
			return lerp(producer.solarCollector.angleIncidenceNS40, producer.solarCollector.angleIncidenceNS50, t);
		case 5:
			return lerp(producer.solarCollector.angleIncidenceNS50, producer.solarCollector.angleIncidenceNS60, t);
		case 6:
			return lerp(producer.solarCollector.angleIncidenceNS60, producer.solarCollector.angleIncidenceNS70, t);
		case 7:
			return lerp(producer.solarCollector.angleIncidenceNS70, producer.solarCollector.angleIncidenceNS80, t);
		case 8:
			return lerp(producer.solarCollector.angleIncidenceNS80, producer.solarCollector.angleIncidenceNS90, t);
		default:
			return 0;
		}
	}
	
	private static double lerp(double a, double b, double t)
	{
		return (1 - t) * a + t * b;
	}
	
	private boolean isHourInInterval(int hour, int[] interval)
	{
		return hour >= interval[0] && hour <= interval[1];
	}
	
	public SolarCalcOperationMode getOperationMode()
	{
		return operationMode;
	}

	public SolarCalcPhase getPhase()
	{
		return phase;
	}
	
	public double getTK_i()
	{
		return TK_i;
	}
	
	public double getAvailablePowerInKWh() {
		return QS_i / 1000 * producer.utilisationRate;
	}
	
	public int getNumStagnationDays()
	{
		return numStagnationDays;
	}
}
