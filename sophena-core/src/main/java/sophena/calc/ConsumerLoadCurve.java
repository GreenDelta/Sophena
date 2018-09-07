package sophena.calc;

import sophena.model.Consumer;
import sophena.model.FuelConsumption;
import sophena.model.HoursTrace;
import sophena.model.LoadProfile;
import sophena.model.Stats;
import sophena.model.TimeInterval;
import sophena.model.WeatherStation;

public class ConsumerLoadCurve {

	private Consumer consumer;
	private WeatherStation station;

	private ConsumerLoadCurve() {
	}

	public static LoadProfile calculate(Consumer consumer,
			WeatherStation station) {
		ConsumerLoadCurve curve = new ConsumerLoadCurve();
		if (consumer == null)
			return LoadProfile.initEmpty();
		if (consumer.profile != null)
			return consumer.profile.clone();
		curve.consumer = consumer;
		curve.station = station;
		return curve.calc();
	}

	private LoadProfile calc() {
		LoadProfile profile = LoadProfile.initEmpty();
		if (consumer == null || station == null)
			return profile;
		if (consumer.demandBased) {
			double totalHeat = consumer.loadHours * consumer.heatingLoad;
			calcCurve(totalHeat, profile);
		} else {
			double totalHeat = 0;
			for (FuelConsumption c : consumer.fuelConsumptions)
				totalHeat += c.getUsedHeat();
			calcCurve(totalHeat, profile);
		}
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
			HoursTrace.applyInterval(
					trace, interval, (old, i) -> true);
		}
		return trace;
	}

}
