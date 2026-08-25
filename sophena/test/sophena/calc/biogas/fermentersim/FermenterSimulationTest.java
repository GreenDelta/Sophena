package sophena.calc.biogas.fermentersim;

import static org.junit.Assert.*;

import org.junit.Test;


public class FermenterSimulationTest {

	@Test
	public void testSimulationResults() {
		var station = TestWeatherStation.get();
		var parameters = FermenterParameters.createDefault(station);
		double[] dataBefore = station.data.clone();
		double[] beamBefore = station.directRadiation.clone();
		double[] diffuseBefore = station.diffuseRadiation.clone();

		var input = SimulationInput.constant(station, 3.5, 1875.0);
		var simulation = new FermenterSimulation();
		var result = simulation.run(parameters, input);

		assertEquals(8760, result.steps().size());
		assertEquals(763.991697, result.totalEnergyMWh(), 1e-4);
		assertEquals(130.440041, result.peakHeatingPowerKW(), 1e-4);
		assertArrayEquals(dataBefore, station.data, 0.0);
		assertArrayEquals(beamBefore, station.directRadiation, 0.0);
		assertArrayEquals(diffuseBefore, station.diffuseRadiation, 0.0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testMissingRadiationDataThrows() {
		var station = TestWeatherStation.get();
		station.directRadiation = null;
		SimulationInput.constant(station, 3.5, 1875.0);
	}
}
