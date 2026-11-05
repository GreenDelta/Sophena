package sophena.calc.biogas.fermentersim;

import static org.junit.Assert.*;

import org.junit.Test;


public class FermenterSimulationTest {

	/// The rated power of the test plant in kW. The CHP of the test plant has
	/// an electric power of 500 kW; with a rated power of 500 kW it runs the
	/// whole year under full load.
	private static final double RATED_POWER = 500;

	@Test
	public void testSimulationResults() {
		var station = TestWeatherStation.get();
		var plant = TestBiogasPlant.get(station);
		var result = FermenterSimulation.of(plant, station, RATED_POWER)
			.orElseThrow()
			.run()
			.orElseThrow();
		assertEquals(8760, result.steps().size());
		assertEquals(764.457948, result.totalEnergyMWh(), 1e-4);
		assertEquals(130.518487, result.peakHeatingPowerKW(), 1e-4);
	}

	@Test
	public void testImmutableInput() {
		var station = TestWeatherStation.get();
		var plant = TestBiogasPlant.get(station);

		double[] dataBefore = station.data.clone();
		double[] beamBefore = station.directRadiation.clone();
		double[] diffuseBefore = station.diffuseRadiation.clone();

		FermenterSimulation.of(plant, station, RATED_POWER)
			.orElseThrow()
			.run()
			.orElseThrow();

		assertArrayEquals(dataBefore, station.data, 0.0);
		assertArrayEquals(beamBefore, station.directRadiation, 0.0);
		assertArrayEquals(diffuseBefore, station.diffuseRadiation, 0.0);
	}

	@Test
	public void testInvalidRatedPower() {
		var station = TestWeatherStation.get();
		var plant = TestBiogasPlant.get(station);
		assertTrue(FermenterSimulation.of(plant, station, -1).isError());
		assertTrue(FermenterSimulation.of(plant, station, Double.NaN).isError());
	}
}
