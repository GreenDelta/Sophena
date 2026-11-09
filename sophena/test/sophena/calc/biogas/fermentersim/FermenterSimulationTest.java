package sophena.calc.biogas.fermentersim;

import static org.junit.Assert.*;

import org.junit.Test;

import sophena.model.biogas.RoofType;


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
		assertEquals(1, result.fermenters().size());
		assertEquals(8760, result.fermenters().getFirst().steps().size());
		assertEquals(8760, result.heatDemand().length);
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

	@Test
	public void testRatedPowerOnlyRequiredForMembraneRoofs() {
		var station = TestWeatherStation.get();
		var plant = TestBiogasPlant.get(station);
		assertTrue(FermenterSimulation.of(plant, station, 0).isError());
		plant.fermenters.getFirst().roofType = RoofType.FIXED;
		assertTrue(FermenterSimulation.of(plant, station, 0).isOk());
	}

	@Test
	public void testHeatDemandIsTheSumOfTheFermenters() {
		var station = TestWeatherStation.get();
		var plant = TestBiogasPlant.get(station);
		var second = FermenterNetworkTest.fermenter("Nachgärer", 38, 2000);
		plant.fermenters.getFirst().successorId = second.id;
		plant.fermenters.add(second);

		var result = run(plant, station);
		assertEquals(2, result.fermenters().size());
		for (int h = 0; h < 8760; h++) {
			double sum = 0;
			for (var f : result.fermenters()) {
				sum += f.steps().get(h).qHeatKW();
			}
			assertEquals(sum, result.heatDemand()[h], 1e-9);
		}
		double total = 0;
		for (var f : result.fermenters()) {
			total += f.totalEnergyMWh();
		}
		assertEquals(total, result.totalEnergyMWh(), 1e-9);
		assertTrue(result.totalEnergyMWh() > 764.457948);
	}

	@Test
	public void testSerialSuccessorWithSameTemperatureHasNoFeedHeat() {
		var station = TestWeatherStation.get();
		var plant = TestBiogasPlant.get(station);
		var first = plant.fermenters.getFirst();
		var second = FermenterNetworkTest.fermenter("Nachgärer", 38, 2000);
		first.successorId = second.id;
		plant.fermenters.add(second);

		var result = run(plant, station);
		var r1 = result.fermenters().get(0);
		var r2 = result.fermenters().get(1);
		assertSame(first, r1.fermenter());
		assertSame(second, r2.fermenter());

		// the first fermenter heats the full substrate input as before
		var single = run(TestBiogasPlant.get(station), station)
			.fermenters().getFirst();
		for (int h = 0; h < 8760; h++) {
			assertEquals(0, r2.steps().get(h).qFeedW(), 1e-9);
			assertEquals(single.steps().get(h).qFeedW(),
				r1.steps().get(h).qFeedW(), 1e-6);
		}
	}

	@Test
	public void testParallelFermentersHeatTheSameFeed() {
		// two parallel fermenters with the same target temperature need the
		// same heat for the substrate as one fermenter with the full input
		var station = TestWeatherStation.get();
		var plant = TestBiogasPlant.get(station);
		var first = plant.fermenters.getFirst();
		var second = FermenterNetworkTest.fermenter(
			"Hauptfermenter 2", 38, first.volume() / 3);
		plant.fermenters.add(second);

		var result = run(plant, station);
		var single = run(TestBiogasPlant.get(station), station)
			.fermenters().getFirst();
		for (int h = 0; h < 8760; h++) {
			double feed = 0;
			for (var f : result.fermenters()) {
				feed += f.steps().get(h).qFeedW();
			}
			assertEquals(single.steps().get(h).qFeedW(), feed, 1e-6);
		}
		// the substrate is shared by volume: 3/4 and 1/4
		var r2 = result.fermenters().get(1);
		assertEquals(0.25 * single.steps().get(0).qFeedW(),
			r2.steps().get(0).qFeedW(), 1e-6);
	}

	@Test
	public void testColderSuccessorHasNoFeedHeat() {
		var station = TestWeatherStation.get();
		var plant = TestBiogasPlant.get(station);
		var second = FermenterNetworkTest.fermenter("Nachgärer", 30, 2000);
		plant.fermenters.getFirst().successorId = second.id;
		plant.fermenters.add(second);
		var r2 = run(plant, station).fermenters().get(1);
		for (var step : r2.steps()) {
			assertEquals(0, step.qFeedW(), 1e-9);
		}
	}

	private static SimulationResult run(
		sophena.model.biogas.BiogasPlant plant,
		sophena.model.WeatherStation station
	) {
		return FermenterSimulation.of(plant, station, RATED_POWER)
			.orElseThrow()
			.run()
			.orElseThrow();
	}
}
