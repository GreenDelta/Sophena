package sophena.calc;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import sophena.calc.biogas.TestPlant;
import sophena.model.Consumer;
import sophena.model.CostSettings;
import sophena.model.HeatNet;
import sophena.model.LoadProfile;
import sophena.model.Producer;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.model.Project;
import sophena.model.Stats;

/// Tests the integration of the biogas results into the project result.
public class ProjectResultBiogasTest {

	@Test
	public void calculatesBiogasResultsFirst() {
		var project = project();
		var producer = biogasProducer(project);

		var r = ProjectResult.calculate(project);

		var biogasResult = r.biogasResults.get(producer);
		assertNotNull(biogasResult);
		assertSame(producer.biogasPlant, biogasResult.plant());

		// the producer profile must be the profile of the biogas result, created
		// with the temperature of the heat net (95 °C is the default)
		var expected = biogasResult.runtime().asProducerProfile(
			producer.biogasPlant, 95.0);
		assertArrayEquals(expected.maxPower, producer.profile.maxPower, 1e-9);
		assertTrue(Stats.max(producer.profile.maxPower) > 0);

		// the product group and the maximum powers are applied as well
		assertSame(producer.biogasPlant.productGroup, producer.productGroup);
		assertEquals(600, producer.profileMaxPower, 1e-9);
		assertEquals(500, producer.profileMaxPowerElectric, 1e-9);
	}

	@Test
	public void recordsErrorsAndResetsTheProfile() {
		var project = project();
		var producer = biogasProducer(project);
		producer.biogasPlant.electricityPrices = null;

		var r = ProjectResult.calculate(project);

		assertTrue(r.biogasResults.isEmpty());
		assertNotNull(producer.profile);
		assertEquals(0, Stats.max(producer.profile.maxPower), 1e-9);

		// a plant that cannot be calculated does not contribute emissions
		assertNull(r.emissionResult.producerEmissions.get(producer));
	}

	@Test
	public void skipsDisabledProducers() {
		var project = project();
		var producer = biogasProducer(project);
		producer.disabled = true;

		var r = ProjectResult.calculate(project);

		assertTrue(r.biogasResults.isEmpty());
		assertNull(producer.profile);
	}

	@Test
	public void addsBiogasEmissionsToTheTotal() {
		var project = project();
		var producer = biogasProducer(project);
		var profile = producer.biogasPlant.substrateProfiles.getFirst();
		profile.annualMass = 10_000;
		profile.substrate.co2Emissions = 100;

		var r = ProjectResult.calculate(project);

		// the emissions of the biogas plant are part of the producer emissions
		var emissions = r.emissionResult;
		assertNotNull(emissions);
		var biogasEmissions = emissions.producerEmissions.get(producer);
		assertNotNull(biogasEmissions);
		assertTrue(biogasEmissions > 0);

		// ... and they are part of the total emissions
		double fromProducers = 0;
		for (var value : emissions.producerEmissions.values()) {
			fromProducers += value;
		}
		assertEquals(
			emissions.electricityEmissions - emissions.electricityCredits
				+ fromProducers,
			emissions.total, 1e-9);
	}

	private static Project project() {
		var project = new Project();
		project.duration = 20;
		project.heatNet = new HeatNet();
		project.costSettings = new CostSettings();

		var consumer = new Consumer();
		consumer.id = "consumer";
		consumer.profile = new LoadProfile();
		consumer.profile.dynamicData = new double[Stats.HOURS];
		consumer.profile.staticData = new double[Stats.HOURS];
		for (int i = 0; i < 2000; i++) {
			consumer.profile.dynamicData[i] = 125;
		}
		project.consumers.add(consumer);
		return project;
	}

	private static Producer biogasProducer(Project project) {
		var plant = TestPlant.of(1600, 4);
		plant.duration = 20;
		plant.productGroup = new ProductGroup();
		plant.productGroup.name = "BHKW";
		plant.productGroup.type = ProductType.COGENERATION_PLANT;

		var producer = new Producer();
		producer.id = "biogas-producer";
		producer.name = "Biogasanlage";
		producer.biogasPlant = plant;
		project.producers.add(producer);
		return producer;
	}
}
