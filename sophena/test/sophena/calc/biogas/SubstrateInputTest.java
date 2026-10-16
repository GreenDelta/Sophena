package sophena.calc.biogas;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.Substrate;
import sophena.model.biogas.SubstrateProfile;

/// Tests the substrate input of a biogas plant.
public class SubstrateInputTest {

	/// 1 ton of the substrate below produces 12.5 m³ methane which is
	/// 124.625 kWh (12.5 m³ x 9.97 kWh/m³).
	private static final double KWH_PER_TONNE = 124.625;

	@Test
	public void emptyPlant() {
		var input = SubstrateInput.of(null);
		assertEquals(0, input.fuelEnergy(), 1e-10);
		assertEquals(0, input.mass(), 1e-10);

		input = SubstrateInput.of(new BiogasPlant());
		assertEquals(0, input.fuelEnergy(), 1e-10);
		assertEquals(0, input.mass(), 1e-10);
	}

	@Test
	public void singleSubstrate() {
		var plant = new BiogasPlant();
		plant.substrateProfiles.add(profile(10_000, 100, 0.3));

		var input = SubstrateInput.of(plant);
		assertEquals(10_000 * KWH_PER_TONNE, input.fuelEnergy(), 1e-6);
		assertEquals(10_000, input.mass(), 1e-10);
		assertEquals(100, input.co2Factor(), 1e-6);
		assertEquals(0.3, input.primaryEnergyFactor(), 1e-6);
	}

	@Test
	public void averagesTheFactorsOverTheEnergy() {
		var plant = new BiogasPlant();
		plant.substrateProfiles.add(profile(10_000, 100, 0.3));
		plant.substrateProfiles.add(profile(30_000, 200, 0.4));

		// the weights are 1/4 for the first and 3/4 for the second substrate
		var input = SubstrateInput.of(plant);
		assertEquals(40_000 * KWH_PER_TONNE, input.fuelEnergy(), 1e-5);
		assertEquals(40_000, input.mass(), 1e-10);
		assertEquals(175, input.co2Factor(), 1e-6);
		assertEquals(0.375, input.primaryEnergyFactor(), 1e-6);
	}

	@Test
	public void countsTheMassOfSubstratesWithoutGas() {
		var plant = new BiogasPlant();
		plant.substrateProfiles.add(profile(10_000, 100, 0.3));
		var noGas = profile(5_000, 100, 0.3);
		noGas.substrate.methaneContent = 0;
		plant.substrateProfiles.add(noGas);

		var input = SubstrateInput.of(plant);
		assertEquals(10_000 * KWH_PER_TONNE, input.fuelEnergy(), 1e-6);
		assertEquals(15_000, input.mass(), 1e-10);
	}

	@Test
	public void skipsProfilesWithoutASubstrate() {
		var plant = new BiogasPlant();
		plant.substrateProfiles.add(new SubstrateProfile());
		plant.substrateProfiles.add(profile(10_000, 100, 0.3));

		var input = SubstrateInput.of(plant);
		assertEquals(10_000 * KWH_PER_TONNE, input.fuelEnergy(), 1e-6);
		assertEquals(10_000, input.mass(), 1e-10);
		assertEquals(100, input.co2Factor(), 1e-6);
	}

	/// Creates a substrate profile for the given annual mass in tons, the given
	/// CO2 emission factor in g CO2 eq./kWh, and the given primary energy
	/// factor. The substrate produces 25 m³ biogas per ton with 50 % methane.
	private static SubstrateProfile profile(
		double mass, double co2Emissions, double primaryEnergyFactor
	) {
		var substrate = new Substrate();
		substrate.dryMatter = 10;
		substrate.organicDryMatter = 50;
		substrate.biogasProduction = 500;
		substrate.methaneContent = 50;
		substrate.co2Emissions = co2Emissions;
		substrate.primaryEnergyFactor = primaryEnergyFactor;

		var profile = new SubstrateProfile();
		profile.substrate = substrate;
		profile.annualMass = mass;
		return profile;
	}
}
