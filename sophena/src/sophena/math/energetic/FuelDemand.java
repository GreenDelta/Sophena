package sophena.math.energetic;

import sophena.model.Boiler;
import sophena.model.Producer;

public class FuelDemand {

	private FuelDemand() {
	}

	/**
	 * Get the amount of fuel energy in [kWh] that is required to produce the
	 * given amount of heat by the given producer.
	 */
	public static double getKWh(Producer producer, double generatedHeat) {
		if (producer == null || producer.boiler == null)
			return generatedHeat;
		if (generatedHeat == 0)
			return 0;
		Boiler boiler = producer.boiler;
		if (!boiler.isCoGenPlant) {
			double ur = UtilisationRate.get(producer, generatedHeat);
			return generatedHeat / ur;
		} else {
			double fullLoadHours = FullLoadHours.get(producer, generatedHeat);
			double er = boiler.efficiencyRateElectric / 100d;
			double p = boiler.maxPowerElectric / er;
			return p * fullLoadHours;
		}
	}

	/**
	 * Get the amount of fuel in the respective fuel unit to produce the given
	 * heat by the given producer.
	 */
	public static double getAmount(Producer producer, double generatedHeat) {
		double cv = CalorificValue.get(producer);
		double energyDemand = getKWh(producer, generatedHeat);
		return cv == 0 ? 0 : energyDemand / cv;
	}
}
