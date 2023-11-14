package sophena.calc;

import sophena.model.Consumer;
import sophena.model.HoursTrace;
import sophena.model.LoadProfile;
import sophena.model.Stats;
import sophena.model.TimeInterval;
import sophena.model.WeatherStation;

public class ConsumerLoadCurve {

	private final Consumer consumer;
	private final WeatherStation station;

	private ConsumerLoadCurve(Consumer consumer, WeatherStation station) {
		this.consumer = consumer;
		this.station = station;
	}

	public static LoadProfile calculate(
			Consumer consumer, WeatherStation station
	){
		if (consumer == null)
			return LoadProfile.initEmpty();
		if (consumer.profile != null)
			return consumer.profile.copy();
		return new ConsumerLoadCurve(consumer, station).calc();
	}

	private LoadProfile calc() {
		var profile = LoadProfile.initEmpty();
		if (consumer == null || station == null)
			return profile;
		double totalHeat;
		if (consumer.demandBased) {
			totalHeat = consumer.loadHours * consumer.heatingLoad;
		} else {
			totalHeat = 0;
			for (var c : consumer.fuelConsumptions) {
				totalHeat += c.getUsedHeat();
			}
		}
		calcCurve(totalHeat, profile);
		return profile;
	}

	private void calcCurve(double totalHeat, LoadProfile profile) {

		// interruptions and operation hours
		boolean[] interruptions = maskInterruptions();
		int operationHours = 0;
		for (int i = 0; i < Stats.HOURS; i++) {
			if (!interruptions[i]) {
				operationHours++;
			}
		}

		// static load for water heating
		double heatForWater = totalHeat * consumer.waterFraction / 100;
		double pStat = operationHours > 0
				? heatForWater / operationHours
				: 0.0;

		// dynamic heat: heating degrees
		double[] heatingDegrees = new double[Stats.HOURS];
		double totalHeatingDegrees = 0.0;
		double tLim = consumer.heatingLimit;
		double tAfr = 4;
		if (consumer.buildingState != null) {
			tAfr = consumer.buildingState.antifreezingTemperature;
		}
		for (int i = 0; i < Stats.HOURS; i++) {
			double limit = interruptions[i] ? tAfr : tLim;
			double temperature = station.data[i];
			if (temperature < limit) {
				double degs = limit - temperature;
				heatingDegrees[i] = degs;
				totalHeatingDegrees += degs;
			}
		}

		// fill the load curve
		double heatPerDegree = totalHeatingDegrees > 0
				? (totalHeat - heatForWater) / totalHeatingDegrees
				: 0.0;
		for (int i = 0; i < Stats.HOURS; i++) {
			if (!interruptions[i]) {
				profile.staticData[i] = pStat;
			}
			profile.dynamicData[i] = heatingDegrees[i] * heatPerDegree;
		}
	}

	private boolean[] maskInterruptions() {
		boolean[] trace = new boolean[Stats.HOURS];
		for (TimeInterval time : consumer.interruptions) {
			int[] interval = HoursTrace.getDayInterval(time);
			HoursTrace.applyInterval(trace, interval, (old, i) -> true);
		}
		return trace;
	}

}
