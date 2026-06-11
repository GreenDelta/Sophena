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
					profile.maxPower[h] += power / 8;
					profile.temperaturLevel[h] = temperature;
				}

				// if we are after a block -> 1/8 ramp-down
				if (h > 0 && runFlags[h - 1]) {
					profile.maxPower[h] += power / 8;
					profile.temperaturLevel[h] = temperature;
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
			if (runTime == 0) {
				// ramp-up
				storage.runHours(0.125);
			}
			storage.runOneHour();
			runTime++;
			runFlags[hour] = true;
		}

		private void stop() {
			if (runTime > 0) {
				// ramp-down
				storage.runHours(0.125);
			}
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
				if (hour < (Stats.HOURS - 1) && !storage.canAdd(profile, hour + 1)) {
					runAt(hour);
					continue;
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

				// otherwise stop it
				stop();
			}

			return new BiogasPlantResult(plant, profile, storage.size(), runFlags);
		}

		private boolean canStartAt(int startHour) {
			int endHour = startHour + minRunTime - 1;
			if (endHour >= Stats.HOURS) {
				return false;
			}
			var s = storage.copy();
			for (int h = startHour; h <= endHour; h++) {
				s.add(profile, h);
				double time = 1.0;
				// start and end could be the same when minRunTime = 1
				if (h == startHour) {
					time += 0.125;
				}
				if (h == endHour) {
					time += 0.125;
				}
				if (s.canRunHours(time)) {
					s.runHours(time);
				} else {
					return false;
				}
			}
			return true;
		}
	}
}
