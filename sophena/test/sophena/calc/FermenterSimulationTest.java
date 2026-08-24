package sophena.calc;

import static org.junit.Assert.*;

import org.junit.Test;

import sophena.calc.biogas.fermentersim.FermenterConstants;
import sophena.calc.biogas.fermentersim.FermenterMaterials;
import sophena.calc.biogas.fermentersim.FermenterParameters;
import sophena.calc.biogas.fermentersim.FermenterSimulation;
import sophena.calc.biogas.fermentersim.SimulationInput;
import sophena.calc.biogas.fermentersim.SyntheticDataGenerator;


public class FermenterSimulationTest {

	@Test
	public void testSimulationResults() {
		var station = SyntheticDataGenerator.generateStation();
		var parameters = FermenterParameters.createDefault(station);
		var materials = FermenterMaterials.createDefault();
		var constants = FermenterConstants.createDefault();
		double[] dataBefore = station.data.clone();
		double[] beamBefore = station.directRadiation.clone();
		double[] diffuseBefore = station.diffuseRadiation.clone();

		var input = SimulationInput.constant(station, 3.5, 1875.0);
		var simulation = new FermenterSimulation();
		var result = simulation.run(parameters, materials, constants, input);

		assertEquals(8760, result.steps().size());
		assertEquals(763.991697, result.totalEnergyMWh(), 1e-4);
		assertEquals(130.440041, result.peakHeatingPowerKW(), 1e-4);
		assertArrayEquals(dataBefore, station.data, 0.0);
		assertArrayEquals(beamBefore, station.directRadiation, 0.0);
		assertArrayEquals(diffuseBefore, station.diffuseRadiation, 0.0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMissingRadiationDataThrows() {
		var station = SyntheticDataGenerator.generateStation();
		var parameters = FermenterParameters.createDefault(station);
		var materials = FermenterMaterials.createDefault();
		var constants = FermenterConstants.createDefault();

		station.directRadiation = null;

		SimulationInput.constant(station, 3.5, 1875.0);
	}
}
