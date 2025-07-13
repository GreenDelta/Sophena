package sophena.calc.biogas;

import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;

public record BiogasPlantResult(
		BiogasPlant plant,
		BiogasProfile biogasProfile,
		double gasStorage
) {

	public static BiogasPlantResult calculate(BiogasPlant plant) {
		if (plant == null
				|| plant.product == null
				|| plant.product.maxPowerElectric <= 0
				|| plant.product.efficiencyRateElectric <= 0
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
			var profile = BiogasProfile.of(plant.substrateProfiles);
			var store = new GasStorage(profile, plant);
			return new BiogasPlantResult(plant, profile, store.size);
		}


	}

	private static class GasStorage {

		private final double size;
		private final BiogasProfile profile;
		private final BiogasPlant plant;
		private final int minHours = 2; // TODO take from plant

		private final double[] track = new double[Stats.HOURS];
		private double filled;
		private double methaneContent;

		GasStorage(BiogasProfile profile, BiogasPlant plant) {
			size = sizeOf(profile);
			this.profile = profile;
			this.plant = plant;
		}

		/// The default size of the gas storage is the maximum amount of biogas
		/// that is produced over a day.
		private double sizeOf(BiogasProfile profile) {
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

		void fill(int hour) {
			double deltaVol = profile.volumeAt(hour);
			double deltaMet = profile.methaneContentAt(hour);
			double nextVol = filled + deltaVol;
			if (nextVol == 0)
				return ;
			methaneContent = (methaneContent * filled + deltaMet * deltaVol) / nextVol;
			filled = nextVol;
		}

		/// With the current methane content, which amount of biogas is required
		/// to run the plant under full load for one hour. A value `< 0` means that
		/// the storage is empty
		double demandPerHour() {
			if (methaneContent <= 0)
				return -1;
			double q = plant.product.maxPowerElectric
					/ plant.product.efficiencyRateElectric;
			return (q / 9.97) / methaneContent;
		}

		///
		boolean canRun() {
			return filled >= demandPerHour();
		}

		boolean canStart() {
			return false; // look ahead min-time!
		}


	}
}
