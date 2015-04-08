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

	public static double[] calculate(Consumer consumer, WeatherStation station) {
		ConsumerLoadCurve curve = new ConsumerLoadCurve();
		curve.consumer = consumer;
		curve.station = station;
		return curve.calc();
	}

	private double[] calc() {
		double[] data = new double[Stats.HOURS];
		if (consumer == null || station == null)
			return data;
		if (consumer.isDemandBased()) {
			double pMax = consumer.getHeatingLoad();
			calcDemandBased(data, pMax);
		} else {
			calcUsageBased(data);
		}
		addLoadProfiles(data);
		return data;
	}

	private void calcUsageBased(double[] data) {
		double usedHeat = 0;
		for (FuelConsumption c : consumer.getFuelConsumptions())
			usedHeat += c.getUsedHeat();
		double heatForWater = usedHeat * consumer.getWaterFraction() / 100;
		double pMin = heatForWater / Stats.HOURS;
		double heatingDegrees = 0;
		double tmax = consumer.getHeatingLimit();
		for (double temperature : station.getData()) {
			if (temperature < tmax)
				heatingDegrees += (tmax - temperature);
		}
		double heatPerHeatingDegree = (usedHeat - heatForWater)
				/ heatingDegrees;
		for (int i = 0; i < Stats.HOURS; i++) {
			double temperature = station.getData()[i];
			if (temperature < tmax)
				data[i] = pMin + ((tmax - temperature) * heatPerHeatingDegree);
			else
				data[i] = pMin;
		}
	}

	private void calcDemandBased(double[] data, double pMax) {
		if (pMax == 0)
			return;
		double pMin = pMax * consumer.getWaterFraction() / 100;
		double[] climateData = station.getData();
		double tmin = Stats.min(climateData);
		double tmax = consumer.getHeatingLimit();
		for (int h = 0; h < Stats.HOURS; h++) {
			double t = climateData[h];
			if (t >= tmax)
				data[h] = pMin;
			else {
				double p = pMin + (pMax - pMin) * (tmax - t) / (tmax - tmin);
				data[h] = p;
			}
		}
	}

	private void addLoadProfiles(double[] data) {
		for (LoadProfile profile : consumer.getLoadProfiles()) {
			double[] p = profile.getData();
			if (p == null)
				continue;
			for (int i = 0; i < Stats.HOURS; i++) {
				data[i] += p[i];
			}
		}
	}
}
