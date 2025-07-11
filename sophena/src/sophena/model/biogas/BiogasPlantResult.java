package sophena.model.biogas;

public record BiogasPlantResult(
		BiogasPlant plant,
		BiogasProfile biogasProfile,
		double gasStorage
) {

	public static BiogasPlantResult calculate(BiogasPlant plant) {
		if (plant == null
				|| plant.product == null
				|| plant.substrateProfiles.isEmpty())
			return emptyOf(plant);
		return new Calculator(plant).run();
	}

	private static BiogasPlantResult emptyOf(BiogasPlant plant) {
		return new BiogasPlantResult(plant, BiogasProfile.empty(), 0);
	}

	private static class Calculator {

		private final BiogasPlant plant;

		Calculator(BiogasPlant plant) {
			this.plant = plant;
		}

		BiogasPlantResult run() {
			var gasProfile = BiogasProfile.of(plant.substrateProfiles);
			double gasStorage = getGasStorage(gasProfile);
			return new BiogasPlantResult(plant, gasProfile, gasStorage);
		}

		/// The default size of the gas storage is the maximum amount of biogas
		/// that is produced over a day.
		private double getGasStorage(BiogasProfile profile) {
			var vol = profile.volume();
			double storage = 0;
			for (int day = 0; day < 365; day++) {
				double daySum = 0;
				int offset = day * 24;
				for (int h = offset; h < offset + 24; h++) {
					daySum += vol[h];
				}
				storage = Math.max(storage, daySum);
			}
			return storage;
		}
	}
}
