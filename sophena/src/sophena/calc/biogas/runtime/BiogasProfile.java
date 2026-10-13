package sophena.calc.biogas.runtime;

import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;

public record BiogasProfile(double[] volume, double[] methaneContent) {

	public static BiogasProfile empty() {
		return new BiogasProfile(
			new double[Stats.HOURS],
			new double[Stats.HOURS]
		);
	}

	public static BiogasProfile of(BiogasPlant plant) {
		return plant == null || plant.substrateProfiles.isEmpty()
			? empty()
			: new Builder(plant).build();
	}

	private static class Builder {

		private final BiogasPlant plant;
		private final double volumeFactor;

		Builder(BiogasPlant plant) {
			this.plant = plant;

			// we apply the methane-slip directly on the volume before it
			// goes into the storage simulation;
			// it is called methane-slip, but is applied on the total volume
			// not only on the methane fraction
			var settings = plant.settings;
			double slipFactor = settings != null && settings.methaneSlip > 0
				? Math.max(0, 1 - (settings.methaneSlip / 100.0))
				: 1.0;

			// the volume calculated from the substrate properties
			// is the normal volume based on 0°C, 1013.25 mbar;
			// the volume is corrected regarding the temperature
			// and pressure in the gas storage

			// the higher the pressure, the smaller the volume
			double pressureFactor = 1013.25
				/ (1013.25 + plant.gasStorageOverpressure);

			// the higher the temperature, the larger the volume
			double temperatureFactor = (273.15 + plant.gasStorageTemperature)
				/ 273.15;

			this.volumeFactor = slipFactor
				* pressureFactor
				* temperatureFactor
				* (1 / waterVaporCorrectionFactor(plant.gasStorageTemperature));
		}

		BiogasProfile build() {
			double[] volume = new double[Stats.HOURS];
			double[] methaneContent = new double[Stats.HOURS];
			for (int h = 0; h < Stats.HOURS; h++) {
				double vol = 0;
				double met = 0;
				for (var p : plant.substrateProfiles) {
					var sub = p.substrate;
					if (sub == null)
						continue;
					double tonnes = Stats.get(p.hourlyValues, h);
					double vi = tonnes
						* (sub.dryMatter / 100)
						* (sub.organicDryMatter / 100)
						* sub.biogasProduction;
					if (vi == 0)
						continue;
					double mi = sub.methaneContent / 100;
					if (vol == 0) {
						vol = vi;
						met = mi;
					} else {
						met = (vol * met + vi * mi) / (vol + vi);
						vol = vol + vi;
					}
				}
				volume[h] = volumeFactor * vol;
				methaneContent[h] = met;
			}
			return new BiogasProfile(volume, methaneContent);
		}

	}

	public double volumeAt(int h) {
		return Stats.get(volume, h);
	}

	public double methaneContentAt(int h) {
		return Stats.get(methaneContent, h);
	}

	static double waterVaporCorrectionFactor(double temperature) {
		return switch ((int) temperature) {

			// exact value for typical temperatures
			case 5 -> 0.991;
			case 10 -> 0.988;
			case 15 -> 0.984;
			case 20 -> 0.978;
			case 25 -> 0.971;
			case 30 -> 0.962;
			case 35 -> 0.951;
			case 40 -> 0.936;
			case 45 -> 0.919;

			// regression formula for other values
			default -> 1.3037E-10 * Math.pow(temperature, 6)
				- 0.000000018735 * Math.pow(temperature, 5)
				+ 0.00000103601 * Math.pow(temperature, 4)
				- 0.0000281476 * Math.pow(temperature, 3)
				+ 0.000362466 * Math.pow(temperature, 2)
				- 0.0027157 * temperature
				+ 0.998444;
		};
	}

}
