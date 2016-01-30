package sophena.math.costs;

import sophena.math.energetic.FuelDemand;
import sophena.model.CostSettings;
import sophena.model.Producer;

/**
 * Functions for calculating costs related to fuel consumption.
 */
public class FuelCosts {

	private FuelCosts() {
	}

	public static double gross(Producer p, double producedHeat) {
		double net = net(p, producedHeat);
		if (net == 0 || p.fuelSpec == null)
			return 0;
		double vat = 1 + p.fuelSpec.taxRate / 100;
		return net * vat;
	}

	public static double net(Producer producer, double producedHeat) {
		if (producedHeat == 0 || producer == null || producer.fuelSpec == null)
			return 0;
		double amount = FuelDemand.getAmount(producer, producedHeat);
		return amount * producer.fuelSpec.pricePerUnit;
	}

	public static double getPriceChangeFactor(Producer p, CostSettings settings) {
		if (p == null || settings == null)
			return 0;
		if (p.boiler.fuel != null)
			return settings.fossilFuelFactor;
		else
			return settings.bioFuelFactor; // wood fuel
	}
}
