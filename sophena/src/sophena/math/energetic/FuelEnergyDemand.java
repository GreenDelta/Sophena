package sophena.math.energetic;

import sophena.calc.EnergyResult;
import sophena.model.Boiler;
import sophena.model.Producer;

public class FuelEnergyDemand {

	private FuelEnergyDemand() {
	}

	public static double getTotalKWh(EnergyResult result) {
		if (result == null)
			return 0;
		double total = 0;
		for (Producer p : result.producers) {
			double genHeat = result.totalHeat(p);
			total += getKWh(p, genHeat);
		}
		return total;
	}

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
}
