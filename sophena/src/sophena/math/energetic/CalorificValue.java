package sophena.math.energetic;

import sophena.model.Fuel;
import sophena.model.FuelConsumption;
import sophena.model.FuelSpec;
import sophena.model.Producer;
import sophena.model.WoodAmountType;

public class CalorificValue {

	public static double get(Producer p) {
		if (p == null || p.fuelSpec == null)
			return 0d;
		FuelSpec spec = p.fuelSpec;
		Fuel fuel = spec.fuel;
		if (fuel == null)
			return 0d;
		if (spec.woodAmountType == null)
			return fuel.calorificValue;
		// wood fuel
		WoodAmountType type = spec.woodAmountType;
		double waterContent = spec.waterContent / 100;
		double woodMass = getWoodMass(type, fuel, waterContent);
		return forWood(woodMass, waterContent, fuel.calorificValue);
	}

	public static double get(FuelConsumption consumption) {
		if (consumption == null || consumption.fuel == null)
			return 0;
		if (consumption.woodAmountType == null) {
			// normal fuel
			return consumption.fuel.calorificValue;
		}
		// wood fuel
		Fuel woodFuel = consumption.fuel;
		double waterContent = consumption.waterContent / 100;
		double woodMass = getWoodMass(consumption.woodAmountType, woodFuel,
				waterContent);
		return forWood(woodMass, waterContent, woodFuel.calorificValue);
	}

	static double forWood(double woodMass, double waterContent, double calorificValue) {
		return woodMass * ((1 - waterContent) * calorificValue - waterContent * 680);
	}

	private static double getWoodMass(WoodAmountType type, Fuel woodFuel,
			double waterContent) {
		switch (type) {
		case MASS:
			return 1; // t
		case CHIPS:
			return WoodMass
					.ofWoodChips_m3(1)
					.waterContent(waterContent)
					.woodDensity_kg_per_m3(woodFuel.density)
					.get_t();
		case LOGS:
			return WoodMass
					.ofWoodLogs_stere(1)
					.waterContent(waterContent)
					.woodDensity_kg_per_m3(woodFuel.density)
					.get_t();
		default:
			return 0;
		}
	}

}
