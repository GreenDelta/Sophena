package sophena.math.costs;

import java.util.EnumSet;

import sophena.calc.EnergyResult;
import sophena.math.energetic.FuelDemand;
import sophena.model.CostSettings;
import sophena.model.Fuel;
import sophena.model.FuelGroup;
import sophena.model.Producer;

/**
 * Functions for calculating costs related to fuel consumption.
 */
public class FuelCosts {

	private FuelCosts() {
	}

	public static double gross(Producer p, EnergyResult result) {
		double net = net(p, result);
		if (net == 0 || p.fuelSpec == null)
			return 0;
		double vat = 1 + p.fuelSpec.taxRate / 100;
		return net * vat;
	}

	public static double net(Producer producer, EnergyResult result) {
		if (result == null || producer == null || producer.fuelSpec == null)
			return 0;
		double amount = FuelDemand.getAmount(producer, result);
		return amount * producer.fuelSpec.pricePerUnit;
	}

	public static double getPriceChangeFactor(Producer p, CostSettings settings) {
		if (p == null || p.fuelSpec == null || settings == null)
			return settings.fossilFuelFactor;
		Fuel fuel = p.fuelSpec.fuel;
		if (fuel == null || fuel.group == null)
			return settings.fossilFuelFactor;
		EnumSet<FuelGroup> bioGroups = EnumSet.of(
				FuelGroup.BIOGAS, FuelGroup.PELLETS,
				FuelGroup.PLANTS_OIL, FuelGroup.WOOD);
		return bioGroups.contains(fuel.group)
				? settings.bioFuelFactor
				: settings.fossilFuelFactor;
	}
}
