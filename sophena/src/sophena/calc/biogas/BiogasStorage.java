package sophena.calc.biogas;

import sophena.model.Boiler;

public class BiogasStorage {

	private final double size;
	private double filled;
	private double methaneContent;

	/// Creates a new biogas storage of the
	public BiogasStorage(double size) {
		if (size <= 0)
			throw new IllegalArgumentException("size is <= 0: " + size);
		this.size = size;
	}

	public boolean filledVolume() {

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
				filled * this.methaneContent +  volume * methaneContent) / nextVol;
		filled = nextVol;
		return 0;
	}

	public double add(BiogasProfile profile, int hour) {
		return add(profile.volumeAt(hour), profile.methaneContentAt(hour));
	}

	/// Returns the number of hours it takes to complete empty the storage when
	/// running the given boiler under full load.
	public double hoursToEmptyOf(Boiler b) {
		if (b == null)
			return Double.POSITIVE_INFINITY;
		double p = b.maxPowerElectric / b.efficiencyRateElectric;
		double q = filled * methaneContent * 9.97;
		return q / p;
	}



}
