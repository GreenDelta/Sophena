package sophena.calc.biogas;

import java.util.UUID;

import sophena.model.ProducerProfile;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;

public record BiogasPlantResult(
	BiogasPlant plant,
	BiogasProfile biogasProfile,
	double gasStorageSize,
	boolean[] runFlags) {

	public static BiogasPlantResult calculate(BiogasPlant plant) {
		if (!BiogasPlants.hasValidBoilers(plant)
			|| BiogasPlants.totalElectricPower(plant) <= 0
			|| plant.gasStorageSize <= 0
			|| plant.substrateProfiles.isEmpty())
			return emptyOf(plant);
		return new Calculator(plant).run();
	}

	private static BiogasPlantResult emptyOf(BiogasPlant plant) {
		return new BiogasPlantResult(
			plant,
			BiogasProfile.empty(),
			0,
			new boolean[Stats.HOURS]);
	}

	public ProducerProfile asProducerProfile(double temperature) {
		var profile = new ProducerProfile();
		profile.id = UUID.randomUUID().toString();
		profile.minPower = new double[Stats.HOURS];
		profile.maxPower = new double[Stats.HOURS];
		profile.temperaturLevel = new double[Stats.HOURS];
		double power = BiogasPlants.totalThermalPower(plant);
		if (power <= 0 || runFlags == null)
			return profile;

		int n = runFlags.length;
		for (int h = 0; h < n; h++) {
			if (runFlags[h]) {
				profile.maxPower[h] = power;
				profile.temperaturLevel[h] = temperature;
			} else {
				// if we are before a block -> 1/8 ramp-up
				if (h < (n - 1) && runFlags[h + 1]) {
					profile.maxPower[h + 1] += power / 8;
					profile.temperaturLevel[h + 1] = temperature;
				}

				// if we are after a block -> 1/8 ramp-down
				if (h > 0 && runFlags[h - 1]) {
					profile.maxPower[h - 1] += power / 8;
					profile.temperaturLevel[h - 1] = temperature;
				}
			}
		}
		return profile;
	}

	private static class Calculator {

		private final BiogasPlant plant;

		private final int minRunTime;
		private final BiogasProfile profile;
		private final BiogasStorage storage;
		private final ElectricityPriceSchedule priceSchedule;

		private final boolean[] runFlags = new boolean[Stats.HOURS];
		private int runTime = 0;

		Calculator(BiogasPlant plant) {
			this.plant = plant;
			minRunTime = Math.max(1, plant.minimumRuntime);
			profile = BiogasProfile.of(plant.substrateProfiles);
			storage = new BiogasStorage(
				plant.gasStorageSize, BiogasPlants.fullLoadFuelPower(plant));
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
