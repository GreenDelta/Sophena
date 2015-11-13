package sophena.math.energetic;

import sophena.model.Producer;

public class FuelAmountDemand {

	public static double get(Producer producer, double generatedHeat) {
		double cv = CalorificValue.get(producer);
		double energyDemand = FuelEnergyDemand.get(producer, generatedHeat);
		return cv == 0 ? 0 : energyDemand / cv;
	}

}
