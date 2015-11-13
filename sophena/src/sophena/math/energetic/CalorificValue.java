package sophena.math.energetic;

import sophena.model.Boiler;
import sophena.model.Fuel;
import sophena.model.FuelConsumption;
import sophena.model.FuelSpec;
import sophena.model.Producer;
import sophena.model.WoodAmountType;

public class CalorificValue {

	public static double get(Producer producer) {
		if (producer == null || producer.boiler == null)
			return 0;
		Boiler boiler = producer.boiler;
		if (boiler.fuel != null) {
			// normal fuel
			return boiler.fuel.calorificValue;
		}
		// wood fuel
		WoodAmountType type = producer.boiler.woodAmountType;
		FuelSpec spec = producer.fuelSpec;
		if (type == null || spec == null || spec.woodFuel == null)
			return 0;
		Fuel woodFuel = spec.woodFuel;
		double waterContent = spec.waterContent / 100;
		double woodMass = getWoodMass(type, woodFuel, waterContent);
		return WoodFuelEnergy
				.ofWoodMass_kg(woodMass)
				.calorificValue_kWh_per_kg(woodFuel.calorificValue)
				.waterContent(waterContent)
				.get_kWh();
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
		return WoodFuelEnergy
				.ofWoodMass_kg(woodMass)
				.calorificValue_kWh_per_kg(woodFuel.calorificValue)
				.waterContent(waterContent)
				.get_kWh();
	}

	private static double getWoodMass(WoodAmountType type, Fuel woodFuel,
			double waterContent) {
		switch (type) {
		case MASS:
			return 1; // kg
		case CHIPS:
			return WoodMass
					.ofWoodChips_m3(1)
					.waterContent(waterContent)
					.woodDensity_kg_per_m3(woodFuel.density)
					.get_kg();
		case LOGS:
			return WoodMass
					.ofWoodLogs_stere(1)
					.waterContent(waterContent)
					.woodDensity_kg_per_m3(woodFuel.density)
					.get_kg();
		default:
			return 0;
		}
	}

}
