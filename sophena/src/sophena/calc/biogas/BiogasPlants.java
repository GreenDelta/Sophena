package sophena.calc.biogas;

import sophena.calc.costs.InvestmentItem;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.Stats;
import sophena.model.biogas.BiogasInvestmentEntry;
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

	/// The total initial investment of the plant in EUR: the investments of the
	/// boilers plus the initial investments of the investment entries. For a
	/// refurbishment only the share that is spent initially is counted, see
	/// `BiogasInvestmentEntry#refurbishmentShare`.
	public static double totalInvestment(BiogasPlant plant) {
		double sum = 0;
		if (plant == null)
			return sum;
		for (var entry : plant.boilers) {
			if (entry == null || entry.costs == null)
				continue;
			sum += entry.costs.investment;
		}
		for (var entry : plant.investments) {
			sum += initialInvestmentOf(entry);
		}
		return sum;
	}

	/// The total value of the plant investments in EUR: the investments of the
	/// boilers plus the full investments of the investment entries. In contrast
	/// to `totalInvestment` this ignores the refurbishment shares, e.g. for the
	/// calculation of the insurance costs.
	public static double totalInvestmentValue(BiogasPlant plant) {
		double sum = 0;
		if (plant == null)
			return sum;
		for (var entry : plant.boilers) {
			if (entry == null || entry.costs == null)
				continue;
			sum += entry.costs.investment;
		}
		for (var entry : plant.investments) {
			sum += investmentOf(entry);
		}
		return sum;
	}

	/// The full investment in EUR of the given entry. This is the amount that is
	/// spent when the asset is replaced after its lifetime.
	public static double investmentOf(BiogasInvestmentEntry entry) {
		return InvestmentItem.of(entry).investment();
	}

	/// The amount in EUR that is spent initially for the given entry. For a
	/// refurbishment or general overhaul of an existing asset only the defined
	/// share is spent, see `BiogasInvestmentEntry#refurbishmentShare`.
	public static double initialInvestmentOf(BiogasInvestmentEntry entry) {
		return InvestmentItem.of(entry).initialInvestment();
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
		for (var entry : plant.investments) {
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

		// an edit of a plant must always be possible, so we use an empty result
		// when the plant cannot be calculated; the plant editor shows the error
		var res = BiogasRuntimeResult.calculate(plant);
		var result = res.isError()
			? BiogasRuntimeResult.emptyOf(plant)
			: res.value();

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

	/// Returns the gas storage size in m³ that is used for the calculations of
	/// the given plant: the size that was entered by the user or, when it is not
	/// defined, the calculated default size.
	public static double gasStorageSizeOf(BiogasPlant plant) {
		if (plant == null)
			return 0;
		var size = plant.gasStorageSize;
		return size != null && size > 0
			? size
			: defaultGasStorageSizeOf(plant);
	}

	/// Returns the effective gas storage size in m³ that is used by the
	/// calculations: the storage size (see `gasStorageSizeOf`) multiplied by the
	/// filling level of the plant (see `BiogasPlant#gasStorageFillingLevel`).
	///
	/// The filling level is a percentage and is clamped to the range `0..100`:
	/// a value `<= 0` is treated as `100` (a fully filled storage), so that
	/// plants that were created before the filling level existed are still
	/// calculated with their full storage.
	public static double effectiveGasStorageSizeOf(BiogasPlant plant) {
		if (plant == null)
			return 0;
		double level = plant.gasStorageFillingLevel;
		if (level <= 0 || level > 100) {
			level = 100;
		}
		return (level / 100) * gasStorageSizeOf(plant);
	}

	/// Calculates the default gas storage size in m³: the maximum volume of
	/// biogas that is produced within 24 hours over the year.
	public static double defaultGasStorageSizeOf(BiogasPlant plant) {
		if (plant == null)
			return 0;
		var profile = BiogasProfile.of(plant).volume();
		if (profile == null)
			return 0;

		double maxSize = 0;
		double nextSize = 0;
		for (int h = 0; h < Stats.HOURS; h++) {
			if (h < 24) {
				nextSize += profile[h];
				continue;
			}

			nextSize = nextSize + profile[h] - profile[h - 24];
			maxSize = Math.max(maxSize, nextSize);
		}
		return maxSize;
	}
}
