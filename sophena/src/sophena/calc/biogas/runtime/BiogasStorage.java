package sophena.calc.biogas.runtime;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.openlca.commons.Copyable;

import sophena.calc.biogas.BiogasPlants;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;

@NullMarked
public class BiogasStorage implements Copyable<BiogasStorage> {

	/// The calorific value of biomethane in kWh/m3
	private final double CAL = 9.97;

	/// The profile of the plant that is simulated with this storage. The
	/// storage reads the produced gas and the volume correction factor from
	/// this profile (see `BiogasProfile#volumeFactor()`).
	private final BiogasProfile profile;

	/// The fuel power demand of the plant under full load in kW.
	private final double fullLoadDemand;
	private final double size;
	private double filled;
	private double methaneContent;

	/// Creates a new biogas storage for the given profile and the given size
	/// in m3 that is used by the plant under full load.
	private BiogasStorage(
		BiogasProfile profile, double size, double fullLoadDemand
	) {
		this.profile = profile;
		this.size = size;
		this.fullLoadDemand = fullLoadDemand;
	}

	/// A storage for the given plant with the profile that is built from it.
	public static BiogasStorage of(@Nullable BiogasPlant plant) {
		return plant == null
			? new BiogasStorage(BiogasProfile.empty(), 0, 0)
			: new BiogasStorage(
				BiogasProfile.of(plant),
				BiogasPlants.effectiveGasStorageSizeOf(plant),
				BiogasPlants.fullLoadFuelPower(plant));
	}

	/// A storage for an explicit profile with the given size and fuel demand.
	/// This is used where a synthetic profile is simulated, like the
	/// worst-case profile in `PreCheck`.
	public static BiogasStorage of(
		BiogasProfile profile, double size, double fullLoadDemand
	) {
		return new BiogasStorage(profile, size, fullLoadDemand);
	}

	/// The profile that this storage reads its gas from.
	public BiogasProfile profile() {
		return profile;
	}

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
	public static double[] annualProfileOf(
		@Nullable BiogasPlant plant,
		@Nullable BiogasProfile profile,
		@Nullable boolean[] runFlags
	) {
		var storageProfile = new double[Stats.HOURS];
		if (plant == null || profile == null || runFlags == null)
			return storageProfile;

		var storage = of(
			profile,
			BiogasPlants.effectiveGasStorageSizeOf(plant),
			BiogasPlants.fullLoadFuelPower(plant));
		boolean running = false;
		for (int h = 0; h < Stats.HOURS; h++) {
			storage.add(h);
			boolean runs = h < runFlags.length && runFlags[h];
			if (runs) {
				if (!running) {
					storage.runHours(Demand.RAMP.factor());
				}
				storage.runOneHour();
				running = true;
			} else {
				if (running) {
					storage.runHours(Demand.RAMP.factor());
				}
				running = false;
			}
			storageProfile[h] = storage.filled();
		}
		return storageProfile;
	}

	public double size() {
		return size;
	}

	/// The amount of gas that is currently in the storage in m³.
	public double filled() {
		return filled;
	}

	/// Add the given volume with the given methane content to this storage. The
	/// resulting methane content of the storage is calculated as a weighted
	/// average of the newly added volume and the filled content of the storage.
	/// If this method returns a value `> 0`, the storage is not large enough to
	/// completely take the new volume. The amount that cannot taken by the
	/// storage is returned in that case.
	public double add(double volume, double methaneContent) {
		double nextVol = filled + volume;
		if (nextVol > size) {
			double diff = nextVol - size;
			add(volume - diff, methaneContent);
			return diff;
		}

		if (filled == 0) {
			filled = volume;
			this.methaneContent = methaneContent;
			return 0;
		}

		this.methaneContent = (
			filled * this.methaneContent + volume * methaneContent) / nextVol;
		filled = nextVol;
		return 0;
	}

	public double add(int hour) {
		if (hour >= Stats.HOURS)
			return 0;
		return add(profile.volumeAt(hour), profile.methaneContentAt(hour));
	}

	public boolean canAdd(int hour) {
		if (hour >= Stats.HOURS)
			return false;
		return (filled + profile.volumeAt(hour)) <= size;
	}

	/// The gas in the storage expressed as normal volume. The profile stores
	/// the gas at the conditions of the gas storage, and the calorific value
	/// `CAL` is defined per normal m3, so the stored volume is divided by the
	/// volume correction factor of the profile.
	private double normalVolume() {
		return filled / profile.volumeFactor();
	}

	/// Returns the number of hours it takes to complete empty the storage when
	/// running the boiler under full load.
	public double hoursToEmpty() {
		if (fullLoadDemand <= 0)
			return 0;
		double q = normalVolume() * methaneContent * CAL;
		return q / fullLoadDemand;
	}

	public boolean canRunOneHour() {
		return canRunHours(1);
	}

	public void runOneHour() {
		runHours(1);
	}

	public boolean canRunHours(double hours) {
		if (fullLoadDemand <= 0)
			return false;
		double q = normalVolume() * methaneContent * CAL;
		return q >= (fullLoadDemand * hours);
	}

	public void runHours(double hours) {
		if (fullLoadDemand <= 0 || methaneContent <= 0) {
			return;
		}
		// the demand is given as energy and converted to the volume at the
		// conditions of the gas storage
		double vol = (hours * fullLoadDemand) / (CAL * methaneContent)
			* profile.volumeFactor();
		filled = filled > vol ? filled - vol : 0;
	}

	public boolean canRun(int hour, Demand factor) {
		if (hour >= Stats.HOURS)
			return false;
		double demand = fullLoadDemand * Demand.factorOf(factor);
		if (demand <= 0)
			return false;
		// the produced and stored gas is converted to normal volume for the
		// energy calculation
		double methane = filled * methaneContent
			+ profile.volumeAt(hour) * profile.methaneContentAt(hour);
		double provided = (methane / profile.volumeFactor()) * CAL;
		return provided >= demand;
	}

	public void run(int hour, Demand factor) {
		if (hour >= Stats.HOURS)
			return;
		double demand = fullLoadDemand * Demand.factorOf(factor);
		if (demand <= 0)
			return;

		double profileVol = profile.volumeAt(hour);
		double profileMethaneContent = profile.methaneContentAt(hour);
		double totalVol = filled + profileVol;
		if (totalVol <= 0)
			return;

		double nextMethaneContent = (
			filled * methaneContent + profileVol * profileMethaneContent) / totalVol;
		if (nextMethaneContent <= 0)
			return;

		// the demand is given as energy and converted to the volume at the
		// conditions of the gas storage
		double volNeeded = demand / (CAL * nextMethaneContent)
			* profile.volumeFactor();
		double remainingVol = totalVol - volNeeded;
		if (remainingVol > size) {
			filled = size;
		} else if (remainingVol > 0) {
			filled = remainingVol;
		} else {
			filled = 0;
		}

		this.methaneContent = filled > 0 ? nextMethaneContent : 0;
	}

	public void setEmpty() {
		filled = 0;
	}

	@Override
	public BiogasStorage copy() {
		var copy = new BiogasStorage(profile, size, fullLoadDemand);
		copy.filled = filled;
		copy.methaneContent = methaneContent;
		return copy;
	}
}
