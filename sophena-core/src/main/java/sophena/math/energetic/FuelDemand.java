package sophena.math.energetic;

import sophena.calc.EnergyResult;
import sophena.model.Boiler;
import sophena.model.Producer;
import sophena.model.Project;

public class FuelDemand {

	private FuelDemand() {
	}

	/**
	 * Get the amount of fuel energy in [kWh] that is required to produce the
	 * given amount of heat by the given producer.
	 */
	public static double getKWh(Project project, Producer producer,
			EnergyResult result) {
		if (producer == null || producer.boiler == null || result == null)
			return 0;
		double generatedHeat = result.totalHeat(producer);
		if (generatedHeat == 0)
			return 0;
		Boiler boiler = producer.boiler;
		if (!boiler.isCoGenPlant) {
			double ur = UtilisationRate.get(project, producer, result);
			return generatedHeat / ur;
		} else {
			double fullLoadHours = Producers.fullLoadHours(producer,
					generatedHeat);
			double er = boiler.efficiencyRateElectric;
			double p = boiler.maxPowerElectric / er;
			return p * fullLoadHours;
		}
	}

	/**
	 * Get the amount of fuel in the respective fuel unit to produce the given
	 * heat by the given producer.
	 */
	public static double getAmount(Project project, Producer producer,
			EnergyResult result) {
		if (producer == null || result == null)
			return 0.0;
		double cv = CalorificValue.get(producer.fuelSpec);
		double energyDemand = getKWh(project, producer, result);
		return cv == 0 ? 0 : energyDemand / cv;
	}
}
