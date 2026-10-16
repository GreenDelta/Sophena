package sophena.calc.biogas;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import sophena.model.biogas.BiogasPlant;

/// The substrate input of a biogas plant. The substrates are the fuel of the
/// plant: their energy content is the fuel energy of the CHP and the properties
/// that are related to the fuel energy are calculated as energy-weighted
/// averages over the substrates.
///
/// @param fuelEnergy The energy content of the biogas that is produced from the
///   substrates in kWh per year. This is the fuel energy of the CHP.
/// @param mass The mass of the substrates in tons per year.
/// @param co2Factor The average CO2 emission factor of the substrates in
///   g CO2 eq. per kWh of fuel energy.
/// @param primaryEnergyFactor The average primary energy factor of the
///   substrates (dimensionless).
@NullMarked
public record SubstrateInput(
	double fuelEnergy,
	double mass,
	double co2Factor,
	double primaryEnergyFactor
) {

	/// The calorific value of bio-methane in kWh/m³ under normal conditions.
	private static final double CAL = 9.97;

	public static SubstrateInput of(@Nullable BiogasPlant plant) {
		if (plant == null || plant.substrateProfiles.isEmpty())
			return new SubstrateInput(0, 0, 0, 0);

		double fuelEnergy = 0; // kWh
		double mass = 0; // t/a
		double co2Factor = 0; // g CO2 eq. / kWh
		double primaryEnergyFactor = 0;

		for (var p : plant.substrateProfiles) {
			if (p == null || p.substrate == null)
				continue;

			// the mass of a substrate that is fed into the plant
			mass += p.annualMass;

			// the methane volume that is produced from the substrate in m³
			// per year
			var methaneVolume = p.annualMass
				* (p.substrate.dryMatter / 100)
				* (p.substrate.organicDryMatter / 100)
				* p.substrate.biogasProduction
				* (p.substrate.methaneContent / 100);
			if (methaneVolume == 0)
				continue;

			var addEnergy = methaneVolume * CAL;
			if (fuelEnergy == 0) {
				fuelEnergy = addEnergy;
				co2Factor = p.substrate.co2Emissions;
				primaryEnergyFactor = p.substrate.primaryEnergyFactor;
				continue;
			}

			co2Factor = (co2Factor * fuelEnergy
				+ p.substrate.co2Emissions * addEnergy)
				/ (fuelEnergy + addEnergy);
			primaryEnergyFactor = (primaryEnergyFactor * fuelEnergy
				+ p.substrate.primaryEnergyFactor * addEnergy)
				/ (fuelEnergy + addEnergy);
			fuelEnergy += addEnergy;
		}

		return new SubstrateInput(
			fuelEnergy, mass, co2Factor, primaryEnergyFactor);
	}
}
