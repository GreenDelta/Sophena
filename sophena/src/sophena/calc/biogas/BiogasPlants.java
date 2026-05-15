package sophena.calc.biogas;

import sophena.model.biogas.BiogasPlant;

public final class BiogasPlants {

	private BiogasPlants() {
	}

	public static boolean hasValidBoilers(BiogasPlant plant) {
		if (plant == null || plant.boilers.isEmpty())
			return false;
		for (var entry : plant.boilers) {
			if (entry == null
					|| entry.boiler == null
					|| entry.boiler.maxPowerElectric <= 0
					|| entry.boiler.efficiencyRateElectric <= 0)
				return false;
		}
		return true;
	}

	public static double totalThermalPower(BiogasPlant plant) {
		double sum = 0;
		if (plant == null)
			return sum;
		for (var entry : plant.boilers) {
			if (entry == null || entry.boiler == null)
				continue;
			sum += entry.boiler.maxPower;
		}
		return sum;
	}

	public static double totalElectricPower(BiogasPlant plant) {
		double sum = 0;
		if (plant == null)
			return sum;
		for (var entry : plant.boilers) {
			if (entry == null || entry.boiler == null)
				continue;
			sum += entry.boiler.maxPowerElectric;
		}
		return sum;
	}

	public static double fullLoadFuelPower(BiogasPlant plant) {
		double sum = 0;
		if (plant == null)
			return sum;
		for (var entry : plant.boilers) {
			if (entry == null
					|| entry.boiler == null
					|| entry.boiler.maxPowerElectric <= 0
					|| entry.boiler.efficiencyRateElectric <= 0)
				continue;
			sum += entry.boiler.maxPowerElectric
					/ entry.boiler.efficiencyRateElectric;
		}
		return sum;
	}

	public static double totalInvestment(BiogasPlant plant) {
		double sum = 0;
		if (plant == null)
			return sum;
		for (var entry : plant.boilers) {
			if (entry == null || entry.costs == null)
				continue;
			sum += entry.costs.investment;
		}
		return sum;
	}

	public static double totalOperationHours(BiogasPlant plant) {
		double sum = 0;
		if (plant == null)
			return sum;
		for (var entry : plant.boilers) {
			if (entry == null || entry.costs == null)
				continue;
			sum += entry.costs.operation;
		}
		return sum;
	}
}