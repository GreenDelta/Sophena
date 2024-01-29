package sophena.calc;

import sophena.model.HeatNet;
import sophena.model.Project;
import sophena.model.HoursTrace;
import sophena.model.Producer;

public class SolarCalcState {
	private Project project;
	private Producer producer;
	private SolarCalcPhase phase;
	private SolarCalcOperationMode operationMode;
	private double TK_i_minus_one = 0;
	private double TK_i;
	private double QS_i;
	
	public SolarCalcState(Project project)
	{
		this.project = project;
		phase = SolarCalcPhase.Aufheiz;
		operationMode = SolarCalcOperationMode.PreHeating;
	}
	
	public void CalcHour(HeatNet heatNet, int hour)
	{
		if(producer.solarCollector == null || producer.solarCollectorSpec == null)
			return;

		// Input
		
		// 1
		double ALB = 0.2;

		// m²
		double A = producer.solarCollectorSpec.solarCollectorArea;
		// 1
		double ETA0 = producer.solarCollector.efficiencyRateRadiation;
		// 1
		double KDF = producer.solarCollector.correctionFactor;
		// W/m²/K
		double A1 = producer.solarCollector.heatTransferCoefficient1;
		// W/m²/K²
		double A2 = producer.solarCollector.heatTransferCoefficient2;
		// Wh/m²/K
		double C = producer.solarCollector.heatCapacity;
		// 1
		double EWFOW = getEWFOW(producer.solarCollectorSpec.solarCollectorAlignment);
		// 1
		double EWFNS = getEWFNS(producer.solarCollectorSpec.solarCollectorTilt);

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
		double LG = project.weatherStation.longitude;
		// °
		double MERI = getReferenceLongitude(hour);
		
		// °C
		double TL_i = project.weatherStation.data[hour];
		// W/m²
		double SDI_i = project.weatherStation.directRadiation != null
				? project.weatherStation.directRadiation[hour]
				: 0;
		// W/m²
		double SDF_i = project.weatherStation.diffuseRadiation != null
				? project.weatherStation.diffuseRadiation[hour]
				: 0;

		SolarCalcOperationMode BA_i = operationMode;
		SolarCalcPhase PHASE_i = phase;
		// 1..365
		int JT = 1 + (hour / 24);
		// 1..24
		int TS = hour % 24;

		// kW
		//double P_i		TODO
		// °C
		//double TV_i		TODO
		// °C
		//double TR_i		TODO

		// Calculation

		// rad
		double B = (JT - 1) / 365 * 2 * Math.PI;
		// min
		double E = 229.2 * (0.000075 + 0.001868 * Math.cos(B) - 0.032077 * Math.sin(B) - 0.014615 * Math.cos(2 * B) - 0.04089 * Math.sin(2 * B));
		// h
		double SZ = ((TS - 0.5) * 3600 + E * 60 + 4 * (MERI - LG) * 60) / 3600;
		// °
		double SD = 23.45 * Math.sin((284 + JT) / 365 * 2 * Math.PI);
		// °
		double HW = -180 + SZ * 180 / 12;
		
		// rad
		double SZW = Math.acos(Math.cos(Math.toRadians(LG)) * Math.cos(Math.toRadians(HW)) * Math.cos(SD) + Math.sin(Math.toRadians(LG)) * Math.sin(SD));
		// rad
		double SAW = Math.signum(HW) * Math.abs(Math.acos(
				(Math.cos(SZW) * Math.sin(Math.toRadians(LG)) - Math.sin(SD)) / ( Math.sin(SZW) * Math.cos(Math.toRadians(LG)))
				));
		// rad
		double EWK = Math.acos(
				Math.cos(SZW) * Math.cos(Math.toRadians(NW)) + Math.sin(SZW) * Math.sin(Math.toRadians(NW)) * Math.cos(SAW - Math.toRadians(AUS)) + 0.0000000001
				);
		// rad
		double EWOW = Math.toDegrees(SZW) < 90 && Math.toDegrees(EWK) < 90
				? Math.atan(Math.sin(SZW) * Math.sin(SAW - Math.toRadians(AUS)) / Math.cos(EWK))
				: Math.toDegrees(89.999);
		// rad
		double EWNS = Math.toDegrees(SZW) < 90 && Math.toDegrees(EWK) < 90
				? -Math.atan(Math.tan(SZW) * Math.cos(SAW - Math.toRadians(AUS)) - Math.toRadians(NW))
				: Math.toDegrees(89.990);
		// 1
		double KOF = Math.toDegrees(SZW) < 89 && Math.toDegrees(EWK) < 89
				? Math.cos(EWK) / Math.cos(SZW)
				: 0;
			
		// TODO: AB HIER EINHEITEN GEWURSCHTEL
		
		// 1 (1367 is solar constant in W/m²)
		double RAB = SDI_i / (1367 * (1 + 0.033 * Math.cos(2 * Math.PI * JT / 365)) * Math.cos(SZW));
		
		// W/m²
		double SGK = SDI_i * KOF + SDF_i * RAB * KOF + SDI_i * (1 - RAB) * 0.5 * (1 + Math.cos(NW) * (SDI_i + SDF_i) * ALB * (1 - 0.5) * (1 - Math.cos(NW)));
		
		// W/m²
		double SDIK = SDI_i * KOF;
		
		// W/m²
		double SDFK = SGK - SDIK;
		
		// 1
		double EWF = EWFOW * EWFNS;
		
		// Wh
		QS_i = (ETA0 * EWF * SDIK + ETA0 * KDF * SDFK - A1 * (TK_i_minus_one - TL_i) - A2 * sqr(TK_i_minus_one - TL_i)) * A * 1;
			
		// °C
		TK_i = TK_i_minus_one + QS_i / (A * C);

		operationMode = CalcOperationMode(heatNet, hour, ETA0 * EWF * SDIK + ETA0 * KDF * SDFK); //TODO
		
		// Output
		
		// kWh
		// QS_N_i

		// kWh
		// QS_PL_i
	}
	
	private SolarCalcOperationMode CalcOperationMode(HeatNet heatNet, int hour, double radiationPerSquareMeter)
	{
		switch(producer.solarCollectorSpec.solarCollectorOperatingMode)
		{
		case AUTO_RADIATION:
			return radiationPerSquareMeter > 0.4
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

	private double getReferenceLongitude(int hour)
	{
		boolean isSummertime = false; //TODO
		return isSummertime ? -30 : -15;
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
		int i = (int)(degrees / 10);
		double t = (degrees % 10) / 10.0;
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
	
	//public double QS_i;
	//public double QS_N_i;
	//public double QS_PL_i;
	//public double TK_i;
	
	  
}
