package sophena.calc.costs;

import sophena.calc.ProjectResult;
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

	public static double get(ProjectResult r, Producer p) {
		if (r == null || p == null || p.fuelSpec == null)
			return 0;
		FuelSpec spec = p.fuelSpec;
		double amount = r.fuelUsage.getInFuelUnits(p);
		double price = spec.pricePerUnit;
		double val = amount * price;
		return val;
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

	public static double getAshCosts(ProjectResult r, Producer p) {
		if (p == null)
			return 0d;
		FuelSpec spec = p.fuelSpec;
		if (spec == null || spec.fuel == null
				|| spec.ashCosts <= 0 || spec.fuel.ashContent <= 0)
			return 0d;
		double fuelAmount = r.fuelUsage.getInFuelUnits(p);
		double ashContent = spec.fuel.ashContent / 100;

		// handle non-wood fuels
		if (spec.fuel.group != FuelGroup.WOOD || spec.woodAmountType == null) {
			// we assume that the fuel amount is given in tons
			double val = fuelAmount * ashContent * spec.ashCosts;
			return val;
		}

		double w = spec.waterContent / 100d;

		// handle wood fuels
		double wetTons = 0d;
		if (spec.woodAmountType == WoodAmountType.MASS) {
			wetTons = fuelAmount;
		} else {
			double f = spec.woodAmountType.getFactor();
			double rho = spec.fuel.density / 1000;
			wetTons = fuelAmount * (f * rho / (1 - w));
		}

		double dryTons = (1d - w) * wetTons;

		double val = dryTons * ashContent * spec.ashCosts;
		return val;
	}
}
