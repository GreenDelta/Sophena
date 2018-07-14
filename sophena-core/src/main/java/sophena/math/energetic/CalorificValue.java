package sophena.math.energetic;

import sophena.model.Fuel;
import sophena.model.FuelConsumption;
import sophena.model.FuelSpec;
import sophena.model.WoodAmountType;

public class CalorificValue {

	public static double get(FuelSpec spec) {
		if (spec == null || spec.fuel == null)
			return 0d;
		Fuel fuel = spec.fuel;
		if (spec.woodAmountType == null)
			return fuel.calorificValue;
		// wood fuel
		double waterContent = spec.waterContent / 100.0;
		double woodMass = woodMassFactor(fuel, spec.woodAmountType,
				waterContent);
		return forWood(woodMass, waterContent, fuel.calorificValue);
	}

	public static double get(FuelConsumption c) {
		if (c == null || c.fuel == null)
			return 0;
		if (c.woodAmountType == null) {
			return c.fuel.calorificValue;
		}
		// wood fuel
		double waterContent = c.waterContent / 100;
		double woodMass = woodMassFactor(c.fuel, c.woodAmountType,
				waterContent);
		return forWood(woodMass, waterContent, c.fuel.calorificValue);
	}

	private static double forWood(
			double woodMass,
			double waterContent,
			double calorificValue) {
		return woodMass
				* ((1 - waterContent) * calorificValue - waterContent * 680);
	}

	/**
	 * Calculates the factor for converting a given wood amount into (wet) mass
	 * tons.
	 */
	private static double woodMassFactor(
			Fuel woodFuel,
			WoodAmountType type,
			double waterContent) {
		if (woodFuel == null || type == null)
			return 1.0;
		if (type == WoodAmountType.MASS)
			return 1;
		double f = 1.0; // mass
		if (type == WoodAmountType.CHIPS) {
			f = 0.4;
		} else if (type == WoodAmountType.LOGS) {
			f = 0.7;
		}
		return (f * woodFuel.density / 1000) / (1 - waterContent);
	}
}
