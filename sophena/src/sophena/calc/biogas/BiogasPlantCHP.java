package sophena.calc.biogas;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import sophena.model.biogas.BiogasPlant;

/// The average performance of the CHP blocks of a biogas plant. In the
/// simulation, we always assume that the plant runs under full load, so we only
/// need the maximum electric and thermal power when calculating the runtime
/// profiles.
///
/// @param electricPower The maximum electric power in kW.
/// @param thermalPower The maximum thermal power in kW.
/// @param electricEfficiency The electric efficiency rate (0..1).
/// @param thermalEfficiency The thermal efficiency rate (0..1).
@NullMarked
public record BiogasPlantCHP(
	double electricPower,
	double thermalPower,
	double electricEfficiency,
	double thermalEfficiency
) {

	public boolean isValid() {
		return electricPower > 0
			&& thermalPower > 0
			&& electricEfficiency > 0
			&& thermalEfficiency > 0;
	}

	public static BiogasPlantCHP of(@Nullable BiogasPlant plant) {

		if (plant == null)
			return new BiogasPlantCHP(0, 0, 0, 0);

		// we calculated the efficiency rates of the plant from the respected CHP
		// blocks as weighted averages, using their respective max. electric and
		// thermal power as weight
		double electricRate = 0;
		double thermalRate = 0;
		double electricPower = 0;
		double thermalPower = 0;

		for (var block : plant.boilers) {
			if (block.boiler == null || block.boiler.maxPowerElectric == 0)
				continue;
			var electricPower$ = block.boiler.maxPowerElectric;
			var thermalPower$ = block.boiler.maxPower;
			var electricRate$ = block.boiler.efficiencyRateElectric;
			var thermalRate$ = block.boiler.efficiencyRate;

			if (electricPower == 0) {
				electricPower = electricPower$;
				thermalPower = thermalPower$;
				thermalRate = thermalRate$;
				electricRate = electricRate$;
				continue;
			}

			electricRate = (
				electricPower * electricRate + electricPower$ * electricRate$
			) / (electricPower + electricPower$);
			thermalRate = (
				thermalPower * thermalRate + thermalPower$ * thermalRate$
			) / (thermalPower + thermalPower$);
			electricPower += electricPower$;
			thermalPower += thermalPower$;
		}

		return new BiogasPlantCHP(
			electricPower, thermalPower, electricRate, thermalRate
		);
	}
}
