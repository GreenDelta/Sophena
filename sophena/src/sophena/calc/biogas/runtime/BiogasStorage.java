package sophena.calc.biogas.runtime;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.openlca.commons.Copyable;

import sophena.calc.biogas.BiogasPlants;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;

@NullMarked
public class BiogasStorage implements Copyable<BiogasStorage> {

	/// The calorific value of biomethane in kWh/m³ under standard conditions.
	private static final double CAL = 9.97;

	/// The profile of the plant that is simulated with this storage. The
	/// storage reads the produced gas and the volume correction factor from
	/// this profile (see `BiogasProfile#volumeFactor()`).
	private final BiogasProfile profile;

	/// The fuel demand of the plant under full load in kW.
	private final double fuelDemand;
	private final double size;
	private double filled;
	private double methaneContent;

	/// Creates a new biogas storage for the given profile and the given size
	/// in m3 that is used by the plant under full load.
	private BiogasStorage(
		BiogasProfile profile, double size, double fuelDemand
	) {
		this.profile = profile;
		this.size = size;
		this.fuelDemand = fuelDemand;
	}

	/// A storage for the given plant with the profile that is built from it.
	public static BiogasStorage of(@Nullable BiogasPlant plant) {
		return plant == null
			? new BiogasStorage(BiogasProfile.empty(), 0, 0)
			: new BiogasStorage(
			BiogasProfile.of(plant),
			BiogasPlants.effectiveGasStorageSizeOf(plant),
			fuelDemandOf(plant));
	}

	/// Calculates the fuel demand that the plant needs when running under full
	/// load.
	private static double fuelDemandOf(BiogasPlant plant) {
		double sum = 0;
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
		boolean @Nullable [] runFlags
	) {
		var storageProfile = new double[Stats.HOURS];
		if (plant == null || profile == null || runFlags == null)
			return storageProfile;

		var storage = new BiogasStorage(
			profile,
			BiogasPlants.effectiveGasStorageSizeOf(plant),
			fuelDemandOf(plant));
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

	public double add(int hour) {
		return hour < 0 || hour >= Stats.HOURS
			? 0
			: add(profile.volumeAt(hour), profile.methaneContentAt(hour));
	}

	/// Add the given volume with the given methane content to this storage. The
	/// resulting methane content of the storage is calculated as a weighted
	/// average of the newly added volume and the filled content of the storage.
	/// If this method returns a value `> 0`, the storage is not large enough to
	/// completely take the new volume. The amount that cannot taken by the
	/// storage is returned in that case.
	private double add(double volume, double methaneContent) {
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

	public boolean canAdd(int hour) {
		if (hour >= Stats.HOURS)
			return false;
		return (filled + profile.volumeAt(hour)) <= size;
	}

	/// Returns the number of hours it takes to complete empty the storage when
	/// running the boiler under full load.
	public double hoursToEmpty() {
		return fuelDemand > 0
			? totalEnergyContent() / fuelDemand
			: 0;
	}

	public boolean canRunOneHour() {
		return canRunHours(1);
	}

	public void runOneHour() {
		runHours(1);
	}

	public boolean canRunHours(double hours) {
		return fuelDemand > 0
			&& totalEnergyContent() >= (fuelDemand * hours);
	}

	public void runHours(double hours) {
		if (fuelDemand <= 0) {
			return;
		}
		double vol = volumeOf(hours * fuelDemand, methaneContent);
		filled = filled > vol ? filled - vol : 0;
	}

	public boolean canRun(int hour, Demand factor) {
		if (hour >= Stats.HOURS)
			return false;
		double energyDemand = fuelDemand * Demand.factorOf(factor);
		if (energyDemand <= 0)
			return false;
		double available = totalEnergyContent()
			+ energyOf(profile.volumeAt(hour), profile.methaneContentAt(hour));
		return available >= energyDemand;
	}

	public void run(int hour, Demand factor) {
		if (hour >= Stats.HOURS)
			return;
		double energyDemand = fuelDemand * Demand.factorOf(factor);
		if (energyDemand <= 0)
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

		double volNeeded = volumeOf(energyDemand, nextMethaneContent);
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

	/// The energy density in kWh/m³ at storage conditions for the given methane
	/// content.
	private double energyDensityOf(double methaneContent) {
		return methaneContent > 0
			? (CAL * methaneContent) / profile.volumeFactor()
			: 0;
	}

	/// Calculates the energy content in kWh for the given storage volume in m³
	/// and methane content.
	private double energyOf(double volume, double methaneContent) {
		return volume > 0
			? volume * energyDensityOf(methaneContent)
			: 0;
	}

	/// Calculates the storage volume in m³ for the given energy in kWh
	/// and methane content.
	private double volumeOf(double energy, double methaneContent) {
		double density = energyDensityOf(methaneContent);
		return density > 0
			? energy / density
			: 0;
	}

	/// Calculates the total energy content of the current storage state in kWh.
	private double totalEnergyContent() {
		return energyOf(filled, methaneContent);
	}

	@Override
	public BiogasStorage copy() {
		var copy = new BiogasStorage(profile, size, fuelDemand);
		copy.filled = filled;
		copy.methaneContent = methaneContent;
		return copy;
	}
}
