package sophena.calc;

import sophena.model.Consumer;
import sophena.model.FuelConsumption;
import sophena.model.LoadProfile;
import sophena.model.Stats;
import sophena.model.WeatherStation;

public class ConsumerLoadCurve {

	private Consumer consumer;
	private WeatherStation station;

	private ConsumerLoadCurve() {
	}

	public static double[] calculate(Consumer consumer,
			WeatherStation station) {
		ConsumerLoadCurve curve = new ConsumerLoadCurve();
		curve.consumer = consumer;
		curve.station = station;
		return curve.calc();
	}

	private double[] calc() {
		double[] data = new double[Stats.HOURS];
		if (consumer == null || station == null)
			return data;
		if (consumer.demandBased) {
			double totalHeat = consumer.loadHours * consumer.heatingLoad;
			calcCurve(totalHeat, data);
		} else {
			double totalHeat = 0;
			for (FuelConsumption c : consumer.fuelConsumptions)
				totalHeat += c.getUsedHeat();
			calcCurve(totalHeat, data);
		}
		addLoadProfiles(data);
		return data;
	}

	private void calcCurve(double totalHeat, double[] data) {
		double heatForWater = totalHeat * consumer.waterFraction / 100;
		double pMin = heatForWater / Stats.HOURS;
		double heatingDegrees = 0;
		double tmax = consumer.heatingLimit;
		for (double temperature : station.getData()) {
			if (temperature < tmax)
				heatingDegrees += (tmax - temperature);
		}
		double heatPerHeatingDegree = (totalHeat - heatForWater)
				/ heatingDegrees;
		for (int i = 0; i < Stats.HOURS; i++) {
			double temperature = station.getData()[i];
			if (temperature < tmax)
				data[i] = pMin + ((tmax - temperature) * heatPerHeatingDegree);
			else
				data[i] = pMin;
		}
	}

	private void addLoadProfiles(double[] data) {
		for (LoadProfile profile : consumer.loadProfiles) {
			double[] p = profile.getData();
			if (p == null)
				continue;
			for (int i = 0; i < Stats.HOURS; i++) {
				data[i] += p[i];
			}
		}
	}
}
