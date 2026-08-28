package sophena.calc.biogas.fermentersim;

import static org.junit.Assert.*;

import org.junit.Test;


public class FermenterSimulationTest {

	@Test
	public void testSimulationResults() {
		var station = TestWeatherStation.get();
		var plant = TestBiogasPlant.get(station);
		var result = FermenterSimulation.of(plant, station)
			.orElseThrow()
			.run();
		assertEquals(8760, result.steps().size());
		assertEquals(763.991697, result.totalEnergyMWh(), 1e-4);
		assertEquals(130.440041, result.peakHeatingPowerKW(), 1e-4);
	}

	@Test
	public void testImmutableInput() {
		var station = TestWeatherStation.get();
		var plant = TestBiogasPlant.get(station);

		double[] dataBefore = station.data.clone();
		double[] beamBefore = station.directRadiation.clone();
		double[] diffuseBefore = station.diffuseRadiation.clone();

		FermenterSimulation.of(plant, station)
			.orElseThrow()
			.run();

		assertArrayEquals(dataBefore, station.data, 0.0);
		assertArrayEquals(beamBefore, station.directRadiation, 0.0);
		assertArrayEquals(diffuseBefore, station.diffuseRadiation, 0.0);
	}
}
