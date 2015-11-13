package sophena.math.energetic;

import sophena.model.Boiler;
import sophena.model.Producer;

public class FuelEnergyDemand {

	public static double get(Producer producer, double generatedHeat) {
		if (producer == null || producer.boiler == null)
			return generatedHeat;
		Boiler boiler = producer.boiler;
		if (!boiler.isCoGenPlant) {
			double ur = UtilisationRate.get(producer, generatedHeat);
			return generatedHeat / ur;
		} else {
			double fullLoadHours = FullLoadHours.get(producer, generatedHeat);
			double p = boiler.maxPowerElectric / boiler.efficiencyRateElectric;
			return p * fullLoadHours;
		}
	}
}
