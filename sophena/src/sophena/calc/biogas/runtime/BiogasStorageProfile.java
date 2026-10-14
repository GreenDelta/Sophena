package sophena.calc.biogas.runtime;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.openlca.commons.Res;

import sophena.calc.biogas.Validation;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;

@NullMarked
public class BiogasStorageProfile {

	/// Simulates the storage over one year with the given run flags and returns
	/// the amount of gas in m³ that is in the storage at the end of each hour.
	///
	/// The gas that is produced in an hour is added to the storage first. When
	/// the plant runs in an hour, the gas for the ramp-up (1/8 of an hour under
	/// full load) is taken from the storage in the first hour of a block and the
	/// full load for every following hour. When a block ends, the gas for the
	/// ramp-down (1/8 of an hour under full load) is taken from the storage in
	/// the first hour in which the plant does not run. Gas that does not fit
	/// into the storage is lost.
	public static Res<double[]> of(
		BiogasPlant plant, boolean @Nullable [] runFlags
	) {
		var err = Validation.energetic(plant);
		if (err.isError())
			return err.castError();

		var storageProfile = new double[Stats.HOURS];
		if (runFlags == null)
			return Res.ok(storageProfile);

		var storage = BiogasStorage.of(plant);
		boolean running = false;
		for (int h = 0; h < Stats.HOURS; h++) {
			storage.add(h);
			boolean runs = h < runFlags.length && runFlags[h];
			if (runs != running) {
				storage.runHours(Demand.RAMP.factor());
			}
			if (runs) {
				storage.runOneHour();
			}
			running = runs;
			storageProfile[h] = storage.filled();
		}

		return Res.ok(storageProfile);
	}
}
