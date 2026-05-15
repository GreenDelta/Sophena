package sophena.calc.biogas;

import sophena.model.Copyable;

public class BiogasStorage implements Copyable<BiogasStorage> {

	/// The calorific value of biomethane in kWh/m3
	private final double CAL = 9.97;

	/// The fuel power demand of the plant in kW under full load.
	private final double fuelPower;
	private final double size;
	private double filled;
	private double methaneContent;

	/// Creates a new biogas storage of the given size in m3 that is used by
	/// the biogas plant under full load.
	public BiogasStorage(double size, double fuelPower) {
		if (size <= 0)
			throw new IllegalArgumentException("size is <= 0: " + size);
		if (fuelPower <= 0)
			throw new IllegalArgumentException("invalid boiler configuration");
		this.size = size;
		this.fuelPower = fuelPower;
	}

	public double size() {
		return size;
	}

	/// Returns the filled volume of storage in m3.
	public double filledVolume() {
		return filled;
	}

	public double methaneContent() {
		return methaneContent;
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
		return add(profile.volumeAt(hour), profile.methaneContentAt(hour));
	}

	public boolean canAdd(BiogasProfile profile, int hour) {
		return (filled + profile.volumeAt(hour)) <= size;
	}

	/// Returns the number of hours it takes to complete empty the storage when
	/// running the boiler under full load.
	public double hoursToEmpty() {
		double q = filled * methaneContent * CAL;
		return q / fuelPower;
	}

	/// Returns true if the boiler can run at least one hour under full load with
	/// the biogas that is currently in the storage.
	public boolean canRunOneHour() {
		double q = filled * methaneContent * CAL;
		return q > fuelPower;
	}

	/// Reduces the stored biogas by the amount that is required to run the linked
	/// boiler under full load for one hour.
	public void runOneHour() {
		double vol = (fuelPower / CAL) / methaneContent;
		filled = filled > vol ? filled - vol : 0;
	}

	public void setEmpty() {
		filled = 0;
	}

	@Override
	public BiogasStorage copy() {
		var copy = new BiogasStorage(size, fuelPower);
		copy.filled = filled;
		copy.methaneContent = methaneContent;
		return copy;
	}
}
