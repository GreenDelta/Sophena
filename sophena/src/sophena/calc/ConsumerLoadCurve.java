package sophena.calc;

import sophena.db.Database;
import sophena.model.Consumer;
import sophena.model.FuelConsumption;
import sophena.model.WeatherStation;

public class ConsumerLoadCurve {

	private final int HOURS = 8760;

	private Consumer consumer;
	private WeatherStation station;
	private Database db;

	private ConsumerLoadCurve() {
	}

	public static double[] calculate(Consumer consumer, WeatherStation station,
			Database db) {
		ConsumerLoadCurve curve = new ConsumerLoadCurve();
		curve.consumer = consumer;
		curve.station = station;
		curve.db = db;
		return curve.calc();
	}

	private double[] calc() {
		double[] data = new double[HOURS];
		if (consumer == null || station == null)
			return data;
		if (consumer.isDemandBased()) {
			double pMax = consumer.getHeatingLoad();
			calcLoadValues(data, pMax);
		} else {
			double pMax = getUsageLoad();
			calcLoadValues(data, pMax);
		}
		// TODO: add load curves
		return data;
	}

	private double getUsageLoad() {
		if (consumer.getLoadHours() == 0)
			return 0;
		double pMax = 0;
		for (FuelConsumption c : consumer.getFuelConsumptions())
			pMax += (c.getUsedHeat() / (double) consumer.getLoadHours());
		return pMax;
	}

	private void calcLoadValues(double[] data, double pMax) {
		if (pMax == 0)
			return;
		double pMin = pMax * consumer.getWaterFraction() / 100;
		double[] climateData = station.getData();
		double tn = station.getNormTemperature();
		double tmax = consumer.getHeatingLimit();
		for (int h = 0; h < HOURS; h++) {
			double t = climateData[h];
			if (t >= tmax)
				data[h] = pMin;
			else {
				double p = pMin + (pMax - pMin) * (tmax - t) / (tmax - tn);
				data[h] = p;
			}
		}
	}

}
