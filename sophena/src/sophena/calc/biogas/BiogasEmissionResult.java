package sophena.calc.biogas;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import sophena.model.biogas.BiogasPlant;

@NullMarked
public class BiogasEmissionResult {

	public static double getAllocatedToHeat(@Nullable BiogasResult result) {
		if (result == null)
			return 0;

		double totalEmissions = totalEmissionsOf(result);
		// allocate the emissions to heat according to RED III, see
		// https://eur-lex.europa.eu/legal-content/EN/TXT/PDF/?uri=CELEX:32018L2001
		// page L 328/151
		var eta = EfficiencyRate.of(result.plant());
		if (eta == null)
			return 0;
		// for heat under 150 °C used for heating of buildings
		double carnotEfficiency = 0.3546;
		return (totalEmissions / eta.heat)
			* (carnotEfficiency * eta.heat / (eta.electric + carnotEfficiency * eta.heat));
	}

	/// Calculates the fuel energy and the emission factor from the substrate
	/// profiles and from this the total emissions of the plant. The emission
	/// factor is calculated as weighted average from the factors of the
	/// respective substrates, weighted by their respective contributions to the
	/// fuel energy.
	private static double totalEmissionsOf(BiogasResult result) {
		double fuelEnergy = 0; // kWh
		double emissionFactor = 0; // g CO2 eq. / kWh
		for (var p : result.plant().substrateProfiles) {
			if (p.substrate == null)
				continue;

			// produced methane volume in m3
			var methaneVolume = p.annualMass
				* (p.substrate.dryMatter / 100)
				* (p.substrate.organicDryMatter / 100)
				* p.substrate.biogasProduction
				* (p.substrate.methaneContent / 100);
			if (methaneVolume == 0)
				continue;

			// calorific value of bio-methane: 9.97 kWh/m3
			var addEnergy = methaneVolume * 9.97;

			if (fuelEnergy == 0) {
				fuelEnergy = addEnergy;
				emissionFactor = p.substrate.co2Emissions;
				continue;
			}

			emissionFactor = (
				emissionFactor * fuelEnergy + p.substrate.co2Emissions * addEnergy
			) / (fuelEnergy + addEnergy);
			fuelEnergy += addEnergy;
		}

		// kg CO2 eq.
		return fuelEnergy * emissionFactor / 1000;
	}

	private record EfficiencyRate(double electric, double heat) {

		@Nullable
		static EfficiencyRate of(@Nullable BiogasPlant plant) {

			if (plant == null)
				return null;

			// we calculated the efficiency rates of the plant from
			// the respected CHP blocks as weighted averages, using
			// their max. electric power as weight
			double electric = 0;
			double heat = 0;
			double power = 0;

			for (var block : plant.boilers) {
				if (block.boiler == null || block.boiler.maxPowerElectric == 0)
					continue;
				var power_i = block.boiler.maxPowerElectric;
				var electric_i = block.boiler.efficiencyRateElectric;
				var heat_i = block.boiler.efficiencyRate;

				if (power == 0) {
					power = power_i;
					heat = heat_i;
					electric = electric_i;
					continue;
				}

				heat = (power * heat + power_i * heat_i) / (power + power_i);
				electric = (power * electric + power_i * electric_i) / (power + power_i);
				power += power_i;
			}

			return heat == 0 && electric == 0
				? null
				: new EfficiencyRate(electric, heat);
		}
	}


}
