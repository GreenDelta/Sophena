package sophena.calc.biogas.runtime;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;

/// The hourly biogas profile that is calculated from the substrate inputs of a
/// biogas plant. The methane slip is directly applied on the profile. Also, a
/// volume correction factor based on the temperature and pressure of the gas
/// storage is directly applied.
///
/// @param volume The produced biogas volume for each hour of a year in m³.
/// @param methaneContent The methane content of the biogas produced in the
///   respective hour (between 0 and 1).
/// @param volumeFactor The applied volume correction factor based on the
///   temperature and pressure of the gas-storage.
@NullMarked
public record BiogasProfile(
	double[] volume, double[] methaneContent, double volumeFactor
) {

	public static BiogasProfile empty() {
		return new BiogasProfile(
			new double[Stats.HOURS],
			new double[Stats.HOURS],
			1.0
		);
	}

	public static BiogasProfile of(@Nullable BiogasPlant plant) {
		return plant == null || plant.substrateProfiles.isEmpty()
			? empty()
			: new Builder(plant).build();
	}

	private static class Builder {

		private final BiogasPlant plant;
		private final double volumeFactor;
		private final double slipFactor;

		Builder(BiogasPlant plant) {
			this.plant = plant;

			// we apply the methane-slip directly on the volume before it
			// goes into the storage simulation;
			// it is called methane-slip, but is applied on the total volume
			// not only on the methane fraction
			var settings = plant.settings;
			this.slipFactor = settings != null && settings.methaneSlip > 0
				? Math.max(0, 1 - (settings.methaneSlip / 100.0))
				: 1.0;

			// the volume calculated from the substrate properties
			// is the normal volume based on 0°C, 1013.25 mbar;
			// the volume is corrected regarding the temperature,
			// the pressure and the water vapor in the gas storage

			// the higher the pressure, the smaller the volume
			double pressureFactor = 1013.25
				/ (1013.25 + plant.gasStorageOverpressure);

			// the higher the temperature, the larger the volume
			double temperatureFactor = (273.15 + plant.gasStorageTemperature)
				/ 273.15;

			// the water-vapor correction is normalized to the reference
			// temperature of 0°C, so it is 1 when the gas storage is at the
			// reference temperature and the produced normal volume is kept
			this.volumeFactor = pressureFactor
				* temperatureFactor
				* waterVaporCorrectionFactor(plant.gasStorageTemperature);
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
				volume[h] = volumeFactor * vol * slipFactor;
				methaneContent[h] = met;
			}
			return new BiogasProfile(volume, methaneContent, volumeFactor);
		}

	}

	public double volumeAt(int h) {
		return Stats.get(volume, h);
	}

	public double methaneContentAt(int h) {
		return Stats.get(methaneContent, h);
	}


	static double waterVaporCorrectionFactor(double temperature) {
		if (temperature <= 0)
			return 1.0;

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
