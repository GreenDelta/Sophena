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
		double woodMass = woodMass(fuel, spec.woodAmountType, waterContent);
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
		double woodMass = woodMass(c.fuel, c.woodAmountType, waterContent);
		return forWood(woodMass, waterContent, c.fuel.calorificValue);
	}

	static double forWood(double woodMass, double waterContent,
			double calorificValue) {
		return woodMass
				* ((1 - waterContent) * calorificValue - waterContent * 680);
	}

	/**
	 * Calculates the real (wet) wood mass that goes into the calculation of a
	 * calorific value for a wood fuel.
	 */
	static double woodMass(Fuel woodFuel, WoodAmountType type,
			double waterContent) {
		if (woodFuel == null || waterContent >= 1.0) {
			return 0.0;
		}
		double f = 1.0; // mass
		if (type == WoodAmountType.CHIPS) {
			f = 0.4;
		} else if (type == WoodAmountType.LOGS) {
			f = 0.7;
		}
		return (f * woodFuel.density / 1000) / (1 - waterContent);
	}
}
