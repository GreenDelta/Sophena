package sophena.calc;

import static org.junit.Assert.*;

import org.junit.Test;

import sophena.calc.biogas.fermentersim.FermenterConstants;
import sophena.calc.biogas.fermentersim.FermenterMaterials;
import sophena.calc.biogas.fermentersim.FermenterParameters;
import sophena.calc.biogas.fermentersim.FermenterSimulation;
import sophena.calc.biogas.fermentersim.SyntheticDataGenerator;


public class FermenterSimulationTest {

	@Test
	public void testSimulationResultsMatchPythonModel() {
		var parameters = FermenterParameters.createDefault();
		var materials = FermenterMaterials.createDefault();
		var constants = FermenterConstants.createDefault();

		var input = SyntheticDataGenerator.generate(parameters);
		var simulation = new FermenterSimulation();
		var result = simulation.run(parameters, materials, constants, input);

		assertEquals(8760, result.steps().size());
		assertEquals(761.469824, result.totalEnergyMWh(), 1e-4);
		assertEquals(130.891196, result.peakHeatingPowerKW(), 1e-4);
	}
}
