package sophena.calc.biogas;

import org.openlca.commons.Copyable;

import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;

public class BiogasStorage implements Copyable<BiogasStorage> {

	/// The calorific value of biomethane in kWh/m3
	private final double CAL = 9.97;

	/// The fuel power demand of the plant under full load in kW.
	private final double fullLoadDemand;
	private final double size;
	private double filled;
	private double methaneContent;

	/// Creates a new biogas storage of the given size in m3 that is used by
	/// the biogas plant under full load.
	public BiogasStorage(double size, double fullLoadDemand) {
		this.size = size;
		this.fullLoadDemand = fullLoadDemand;
	}

	public static BiogasStorage of(BiogasPlant plant) {
		var fullLoadDemand = BiogasPlants.fullLoadFuelPower(plant);
		return plant != null
			? new BiogasStorage(plant.gasStorageSize, fullLoadDemand)
			: new BiogasStorage(0, 0);
	}

	public double size() {
		return size;
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

	public double add(BiogasProfile profile, int hour) {
		if (hour >= Stats.HOURS)
			return 0;
		return add(profile.volumeAt(hour), profile.methaneContentAt(hour));
	}

	public boolean canAdd(BiogasProfile profile, int hour) {
		if (hour >= Stats.HOURS)
			return false;
		return (filled + profile.volumeAt(hour)) <= size;
	}

	/// Returns the number of hours it takes to complete empty the storage when
	/// running the boiler under full load.
	public double hoursToEmpty() {
		if (fullLoadDemand <= 0)
			return 0;
		double q = filled * methaneContent * CAL;
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
		double q = filled * methaneContent * CAL;
		return q >= (fullLoadDemand * hours);
	}

	public void runHours(double hours) {
		if (fullLoadDemand <= 0 || methaneContent <= 0) {
			return;
		}
		double vol = (hours * fullLoadDemand) / (CAL * methaneContent);
		filled = filled > vol ? filled - vol : 0;
	}

	public boolean canRun(BiogasProfile profile, int hour, Demand factor) {
		if (profile == null || hour >= Stats.HOURS)
			return false;
		double demand = fullLoadDemand * Demand.factorOf(factor);
		if (demand <= 0)
			return false;
		double methane = filled * methaneContent
			+ profile.volumeAt(hour) * profile.methaneContentAt(hour);
		double provided = methane * CAL;
		return provided >= demand;
	}

	public void run(BiogasProfile profile, int hour, Demand factor) {
		if (profile == null || hour >= Stats.HOURS)
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

		double volNeeded =  demand / (CAL * nextMethaneContent);
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
		var copy = new BiogasStorage(size, fullLoadDemand);
		copy.filled = filled;
		copy.methaneContent = methaneContent;
		return copy;
	}
}
