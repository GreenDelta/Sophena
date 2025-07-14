package sophena.calc.biogas;

import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;

public record BiogasPlantResult(
		BiogasPlant plant,
		BiogasProfile biogasProfile,
		double gasStorageSize,
		boolean[] runFlags
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
		return new BiogasPlantResult(
				plant,
				BiogasProfile.empty(),
				0,
				new boolean[Stats.HOURS]
		);
	}

	private static class Calculator {

		private final BiogasPlant plant;

		private final int minRunTime = 2;  // todo: configure it!
		private final BiogasProfile profile;
		private final BiogasStorage storage;
		private final ElectricityPriceSchedule priceSchedule;

		private final boolean[] runFlags = new boolean[Stats.HOURS];
		private int runTime = 0;


		Calculator(BiogasPlant plant) {
			this.plant = plant;
			profile = BiogasProfile.of(plant.substrateProfiles);
			var storageSize = BiogasStorage.defaultSizeOf(profile);
			storage = new BiogasStorage(storageSize, plant.product);
			priceSchedule = ElectricityPriceSchedule.calculate(plant, profile);
		}

		private void runAt(int hour) {
			storage.runOneHour();
			runTime++;
			runFlags[hour] = true;
		}

		private void stop() {
			runTime = 0;
		}

		BiogasPlantResult run() {

			for (int hour = 0; hour < Stats.HOURS; hour++) {

				storage.add(profile, hour);

				// the storage is empty
				if (!storage.canRunOneHour()) {
					stop();
					continue;
				}

				// the storage is full!
				if (hour < (Stats.HOURS - 1)) {
					if (!storage.canAdd(profile, hour + 1)) {
						runAt(hour);
						continue;
					}
				}

				boolean priceOk = priceSchedule.shouldRunAt(hour);

				// if it is not running, start it only if the price is good and
				// if it can run for the minimum runtime
				if (runTime == 0) {
					if (priceOk && canStartAt(hour)) {
						runAt(hour);
					}
					continue;
				}

				// if it did not run for the minimum runtime or if the price is good,
				// keep it running
				if (priceOk || runTime < minRunTime) {
					runAt(hour);
					continue;
				}

				// also, keep it running if the price is good in the next hour
				int nextHour = hour + 1;
				if (nextHour < Stats.HOURS && priceSchedule.shouldRunAt(nextHour)) {
					var s = storage.copy();
					s.runOneHour();
					s.add(profile, nextHour);
					if (s.canRunOneHour()) {
						runAt(hour);
						continue;
					}
				}

				// otherwise stop it
				stop();
			}

			return new BiogasPlantResult(plant, profile, storage.size(), runFlags);
		}

		private boolean canStartAt(int hour) {
			int end = hour + minRunTime;
			if (end >= Stats.HOURS)
				return false;
			var s = storage.copy();
			for (int h = hour + 1; h < end; h++) {
				s.runOneHour();
				s.add(profile, h);
				if (!s.canRunOneHour())
					return false;
			}
			return true;
		}
	}
}
