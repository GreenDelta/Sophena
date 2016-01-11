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

	public static LoadProfile calculate(Consumer consumer,
			WeatherStation station) {
		ConsumerLoadCurve curve = new ConsumerLoadCurve();
		curve.consumer = consumer;
		curve.station = station;
		return curve.calc();
	}

	private LoadProfile calc() {
		LoadProfile profile = new LoadProfile();
		profile.dynamicData = new double[Stats.HOURS];
		profile.staticData = new double[Stats.HOURS];
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
		addLoadProfiles(profile);
		return profile;
	}

	private void calcCurve(double totalHeat, LoadProfile profile) {
		double heatForWater = totalHeat * consumer.waterFraction / 100;
		double pMin = heatForWater / Stats.HOURS;
		double heatingDegrees = 0;
		double tmax = consumer.heatingLimit;
		for (double temperature : station.data) {
			if (temperature < tmax)
				heatingDegrees += (tmax - temperature);
		}
		double qd = (totalHeat - heatForWater) / heatingDegrees;
		for (int i = 0; i < Stats.HOURS; i++) {
			profile.staticData[i] = pMin;
			double temperature = station.data[i];
			if (temperature < tmax)
				profile.dynamicData[i] = ((tmax - temperature) * qd);
		}
	}

	private void addLoadProfiles(LoadProfile profile) {
		for (LoadProfile p : consumer.loadProfiles) {
			Stats.add(p.dynamicData, profile.dynamicData);
			Stats.add(p.staticData, profile.staticData);
		}
	}

}
