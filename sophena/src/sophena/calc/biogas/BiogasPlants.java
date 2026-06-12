package sophena.calc.biogas;

import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.Stats;
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

	public static boolean isFeedInAllowed(BiogasPlant plant, int hour) {
		if (plant.electricityPrices == null
			|| plant.electricityPrices.feedInAllowed == null)
			return true;
		var allowed = plant.electricityPrices.feedInAllowed;
		return hour < 0 || hour >= allowed.length || allowed[hour];
	}

	public static void syncProducerProfile(Project project, Producer producer) {
		if (project == null || producer == null)
			return;
		var plant = producer.biogasPlant;
		if (plant == null)
			return;
		producer.productGroup = plant.productGroup;
		var result = BiogasPlantResult.calculate(plant);
		double temperature = project.heatNet != null
			&& project.heatNet.maxBufferLoadTemperature > 0
			? project.heatNet.maxBufferLoadTemperature
			: 95;
		producer.profile = result.asProducerProfile(temperature);
		double thermalPower = totalThermalPower(plant);
		producer.profileMaxPower = thermalPower > 0
			? thermalPower
			: Stats.max(producer.profile.maxPower);
		producer.profileMaxPowerElectric = totalElectricPower(plant);
	}

	public static double[] heatDemandOf(Project project, BiogasPlant plant) {
		var demand = new double[Stats.HOURS];
		double power = totalThermalPower(plant);
		if (power < 0.1
			|| project == null
			|| project.weatherStation == null
			|| project.weatherStation.data == null)
			return demand;
		var temperature = project.weatherStation.data;
		double maxDist = 40 - Stats.min(temperature);
		double maxDemand = 0.2 * power;
		for (int h = 0; h < temperature.length; h++) {
			var dist = 40 - temperature[h];
			demand[h] = maxDemand * dist / maxDist;
		}
		return demand;
	}

}
