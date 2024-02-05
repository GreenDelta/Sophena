package sophena.math.energetic;

import sophena.model.HeatNet;
import sophena.model.HoursTrace;
import sophena.model.TimeInterval;

public class SeasonalItem {
	public double targetChargeLevel;
	public double flowTemperature;
	public double returnTemperature;

	public static SeasonalItem calc(HeatNet net, int hour){
		if (net == null)
			return null;
		SeasonalItem seasonalItem = new SeasonalItem();
		if (net.isSeasonalDrivingStyle) {	
			TimeInterval timeIntervalSummerWinter = new TimeInterval();
			timeIntervalSummerWinter.start = net.intervalSummer.end;
			timeIntervalSummerWinter.end = net.intervalWinter.start;
			int[] hourIntervalSummerWinter = HoursTrace.getHourInterval(timeIntervalSummerWinter);
			TimeInterval timeIntervalWinterSummer = new TimeInterval();
			timeIntervalWinterSummer.start = net.intervalWinter.end;
			timeIntervalWinterSummer.end = net.intervalSummer.start;
			int[] hourIntervalWinterSummer = HoursTrace.getHourInterval(timeIntervalWinterSummer);
			
			if(isHourInInterval(hour, HoursTrace.getHourInterval(net.intervalSummer))) {
				seasonalItem.targetChargeLevel = net.targetChargeLevelSummer % 100;
				seasonalItem.flowTemperature = net.flowTemperatureSummer;
				seasonalItem.returnTemperature = net.returnTemperatureSummer;
			}else if(isHourInInterval(hour, HoursTrace.getHourInterval(net.intervalWinter))) {
				seasonalItem.targetChargeLevel = net.targetChargeLevelWinter % 100;
				seasonalItem.flowTemperature = net.flowTemperatureWinter;
				seasonalItem.returnTemperature = net.returnTemperatureWinter;
			}else if (isHourInInterval(hour, hourIntervalSummerWinter)) {
				seasonalItem.targetChargeLevel = linearInterpolation(hourIntervalSummerWinter[0], net.targetChargeLevelSummer, hourIntervalSummerWinter[1], net.targetChargeLevelWinter, hour) % 100;
				seasonalItem.flowTemperature = linearInterpolation(hourIntervalSummerWinter[0], net.flowTemperatureSummer, hourIntervalSummerWinter[1], net.flowTemperatureWinter, hour);
				seasonalItem.returnTemperature = linearInterpolation(hourIntervalSummerWinter[0], net.returnTemperatureSummer, hourIntervalSummerWinter[1], net.returnTemperatureWinter, hour);
			}else {
				seasonalItem.targetChargeLevel = linearInterpolation(hourIntervalWinterSummer[0], net.targetChargeLevelWinter, hourIntervalWinterSummer[1], net.targetChargeLevelSummer, hour) % 100;
				seasonalItem.flowTemperature = linearInterpolation(hourIntervalWinterSummer[0], net.flowTemperatureWinter, hourIntervalWinterSummer[1], net.flowTemperatureSummer, hour);
				seasonalItem.returnTemperature = linearInterpolation(hourIntervalWinterSummer[0], net.returnTemperatureWinter, hourIntervalWinterSummer[1], net.returnTemperatureSummer, hour);
			}			
		} else {
			seasonalItem.targetChargeLevel = 1;
			seasonalItem.flowTemperature = net.supplyTemperature;
			seasonalItem.returnTemperature = net.returnTemperature;
		}
		return seasonalItem;
	}

	private static boolean isHourInInterval(int hour, int[] interval)
	{
		if(interval[0] < interval[1])
		{
			return hour >= interval[0] && hour <= interval[1];
		}
		else
		{
			return !(hour > interval[1] && hour < interval[0]);
		}
	}
	
	private static double linearInterpolation(double startHour, double startValue, double endHour, double endValue, double currentHour)
	{
		return startValue + ((endValue - startValue) / (endHour - startHour)) * (currentHour - startHour);
	}
}
