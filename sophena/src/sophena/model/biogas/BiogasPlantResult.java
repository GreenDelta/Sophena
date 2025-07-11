package sophena.model.biogas;

import sophena.model.Stats;

public record BiogasPlantResult(
		BiogasPlant plant,
		double gasStorage
) {

	public static BiogasPlantResult calculate(BiogasPlant plant) {
		if (plant == null || plant.substrateProfiles.isEmpty())
			return emptyOf(plant);
		return new Calculator(plant).run();
	}

	private static BiogasPlantResult emptyOf(BiogasPlant plant) {
		return new BiogasPlantResult(plant, 0);
	}

	private static class Calculator {

		private final BiogasPlant plant;

		Calculator(BiogasPlant plant) {
			this.plant = plant;
		}

		BiogasPlantResult run() {

			double[] methaneProfile = getMethaneProfile();
			double gasStorage = getGasStorage(methaneProfile);

			return new BiogasPlantResult(plant, gasStorage);
		}

		private double[] getMethaneProfile() {
			double[] sum = new double[Stats.HOURS];
			for (var p : plant.substrateProfiles) {
				var next = p.getMethaneProfile();
				for (int h = 0; h < next.length; h++) {
					sum[h] += next[h];
				}
			}
			return sum;
		}

		private double getGasStorage(double[] methaneProfile) {
			double storage = 0;
			for (int day = 0; day < 365; day++) {
				double daySum = 0;
				int offset = day * 24;
				for (int h = offset; h < offset + 24; h++) {
					daySum += methaneProfile[h];
				}
				storage = Math.max(storage, daySum);
			}
			return storage;
		}
	}
}
