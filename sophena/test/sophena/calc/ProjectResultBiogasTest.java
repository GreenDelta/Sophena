package sophena.calc;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import sophena.calc.biogas.SubstrateInput;
import sophena.calc.biogas.TestPlant;
import sophena.calc.costs.CostResultItem;
import sophena.model.Consumer;
import sophena.model.CostSettings;
import sophena.model.HeatNet;
import sophena.model.LoadProfile;
import sophena.model.Producer;
import sophena.model.ProductCosts;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.model.Project;
import sophena.model.Stats;
import sophena.model.biogas.BiogasInvestmentEntry;

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

	@Test
	public void usesTheSubstrateInputAsFuel() {
		var project = project();
		var producer = biogasProducer(project);
		producer.biogasPlant.substrateProfiles.getFirst().annualMass = 10_000;

		var r = ProjectResult.calculate(project);

		// the fuel of a biogas plant is the input of its substrates; without
		// this the fuel usage would be 0 because the plant has no fuel spec
		double fuelEnergy = r.fuelUsage.getInKWh(producer);
		assertTrue(fuelEnergy > 0);
		assertEquals(SubstrateInput.of(producer.biogasPlant).fuelEnergy(),
			fuelEnergy, 1e-6);
		assertEquals(10_000, r.fuelUsage.getInFuelUnits(producer), 1e-9);
	}

	@Test
	public void addsTheHeatPurchaseToTheProjectCosts() {
		var project = project();
		var producer = biogasProducer(project);
		// the plant needs an investment so that it has a target profit and
		// therefore a suggested heat price
		var entry = new BiogasInvestmentEntry();
		entry.costs = new ProductCosts();
		entry.costs.investment = 5_000_000;
		producer.biogasPlant.investments.add(entry);

		var r = ProjectResult.calculate(project);

		var biogasResult = r.biogasResults.get(producer);
		assertNotNull(biogasResult);
		double dynPrice = biogasResult.costs().dynamicTotal.suggestedHeatPrice;
		double statPrice = biogasResult.costs().staticTotal.suggestedHeatPrice;
		assertTrue(dynPrice > 0);
		assertTrue(statPrice > 0);

		// the network pays for the heat that is used from the producer
		double heat = r.energyResult.totalHeat(producer) / 1000.0; // MWh
		assertTrue(heat > 0);
		assertClose(heat * dynPrice,
			r.costResult.dynamicTotal.consumptionCosts);
		assertClose(heat * statPrice,
			r.costResult.staticTotal.consumptionCosts);

		// the heat purchase is shown for the item of the producer
		var item = itemOf(r, producer);
		assertNotNull(item);
		assertClose(heat * dynPrice, item.demandRelatedCosts());
	}

	/// Returns the cost item of the given producer.
	private static CostResultItem itemOf(ProjectResult r, Producer p) {
		for (var item : r.costResult.items) {
			if (item.investment().producer() == p)
				return item;
		}
		return null;
	}

	private static void assertClose(double expected, double actual) {
		assertEquals(expected, actual,
			Math.max(1e-6, Math.abs(expected) * 1e-9));
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
