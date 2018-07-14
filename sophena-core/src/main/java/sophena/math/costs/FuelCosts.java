package sophena.math.costs;

import sophena.calc.EnergyResult;
import sophena.math.energetic.FuelDemand;
import sophena.model.CostSettings;
import sophena.model.Fuel;
import sophena.model.FuelGroup;
import sophena.model.FuelSpec;
import sophena.model.Producer;
import sophena.model.WoodAmountType;

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

	public static double getPriceChangeFactor(Producer p,
			CostSettings settings) {
		if (p == null || p.fuelSpec == null || settings == null)
			return settings.fossilFuelFactor;
		Fuel fuel = p.fuelSpec.fuel;
		if (fuel == null || fuel.group == null)
			return settings.fossilFuelFactor;
		switch (fuel.group) {
		case BIOGAS:
		case PELLETS:
		case PLANTS_OIL:
		case WOOD:
			return settings.bioFuelFactor;
		case ELECTRICITY:
			return settings.electricityFactor;
		default:
			return settings.fossilFuelFactor;
		}
	}

	public static double netAshCosts(Producer p, EnergyResult result) {
		if (p == null)
			return 0d;
		FuelSpec spec = p.fuelSpec;
		if (spec == null || spec.fuel == null
				|| spec.ashCosts <= 0 || spec.fuel.ashContent <= 0)
			return 0d;
		double fuelAmount = FuelDemand.getAmount(p, result);
		if (fuelAmount == 0)
			return 0d;
		double ashContent = spec.fuel.ashContent / 100;

		// handle non-wood fuels
		if (spec.fuel.group != FuelGroup.WOOD || spec.woodAmountType == null) {
			// we assume that the fuel amount is given in tons
			return fuelAmount * ashContent * spec.ashCosts;
		}

		// handle wood fuels
		double wetTons = 0d;
		if (spec.woodAmountType == WoodAmountType.MASS) {
			wetTons = fuelAmount;
		} else {
			double f = spec.woodAmountType.getFactor();
			wetTons = fuelAmount * (f * (spec.fuel.density / 1000)
					/ (1 - spec.waterContent / 100));
		}
		double dryTons = (1d - (spec.waterContent / 100d)) * wetTons;
		return dryTons * ashContent * spec.ashCosts;
	}
}
