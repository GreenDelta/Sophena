package sophena.calc;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import sophena.calc.biogas.SubstrateInput;
import sophena.calc.biogas.TestPlant;
import sophena.calc.biogas.fermentersim.TestWeatherStation;
import sophena.calc.costs.Annuity;
import sophena.calc.costs.CostResultItem;
import sophena.calc.kpi.EfficiencyResult;
import sophena.calc.kpi.GeneratedElectricity;
import sophena.calc.specs.Producers;
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

		// in the project context the biogas plant does not generate electricity
		assertEquals(0, Producers.electricPower(producer), 1e-9);
		assertFalse(Producers.isCoGenPlant(producer));
		assertEquals(0, GeneratedElectricity.get(producer, r), 1e-9);
		assertEquals(0, GeneratedElectricity.getTotal(r), 1e-9);
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
		// biogas plant does not receive electricity credits in the project
		assertEquals(0, emissions.electricityCredits, 1e-9);
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
		double price = biogasResult.getEffectiveHeatPrice(
			producer.biogasHeatPrice);
		assertTrue(price > 0);

		// the network pays for the heat that is used from the producer. The
		// plant has no fermenter in this project, so the fermenter heat is 0.
		assertEquals(0, r.energyResult.fermenterHeatOf(producer), 1e-9);
		double heat = Math.max(0, r.energyResult.totalHeat(producer)
			- r.energyResult.fermenterHeatOf(producer)) / 1000.0; // MWh
		assertTrue(heat > 0);
		double dynamicCosts = dynamicHeatCosts(project, heat, price);
		assertClose(dynamicCosts,
			r.costResult.dynamicTotal.consumptionCosts);
		assertClose(staticHeatCosts(project, heat, price),
			r.costResult.staticTotal.consumptionCosts);

		// the heat purchase is shown for the item of the producer
		var item = itemOf(r, producer);
		assertNotNull(item);
		assertClose(dynamicCosts, item.demandRelatedCosts());

		// the project does not earn electricity revenues from the biogas plant
		assertEquals(0, r.costResult.dynamicTotal.revenuesElectricity, 1e-9);
		assertEquals(0, r.costResult.staticTotal.revenuesElectricity, 1e-9);
	}

	@Test
	public void subtractsTheFermenterHeatFromTheHeatPurchase() {
		var project = project();
		var producer = biogasProducer(project);
		project.weatherStation = TestWeatherStation.get();
		producer.biogasPlant.fermenter = TestPlant.fermenter();
		// a high-yield substrate reduces the fermenter heat demand while the
		// gas production of the plant stays the same
		var substrate = producer.biogasPlant.substrateProfiles.getFirst();
		substrate.substrate.biogasProduction = 2000;
		Arrays.fill(substrate.hourlyValues, 2.0);
		project.costSettings.heatRevenues = 100;

		// the plant needs an investment so that it has a target profit and
		// therefore a suggested heat price
		var entry = new BiogasInvestmentEntry();
		entry.costs = new ProductCosts();
		entry.costs.investment = 5_000_000;
		producer.biogasPlant.investments.add(entry);

		var r = ProjectResult.calculate(project);

		var biogasResult = r.biogasResults.get(producer);
		assertNotNull(biogasResult);

		// the fermenter heat is calculated per producer and aggregated
		double fermenterHeat = r.energyResult.fermenterHeatOf(producer);
		assertTrue(fermenterHeat > 0);
		assertEquals(r.energyResult.totalFermenterHeatDemand,
			fermenterHeat, 1e-6);

		double producedHeat = r.energyResult.totalHeat(producer);
		double soldHeat = Math.max(0, producedHeat - fermenterHeat);
		assertTrue(soldHeat > 0);
		assertTrue(soldHeat < producedHeat);

		// the heat that the plant can offer but that is not fed into the
		// network is cooled away
		var efficiency = EfficiencyResult.calculate(r);
		double offeredHeat = biogasResult.revenues().totalGeneratedHeat();
		double cooledHeat = Math.max(0, offeredHeat - producedHeat);
		assertTrue(cooledHeat > 0);
		assertClose(cooledHeat, efficiency.cooledBiogasHeat);

		// the network only pays for the sold heat
		double price = biogasResult.getEffectiveHeatPrice(
			producer.biogasHeatPrice);
		double dynamicCosts = dynamicHeatCosts(
			project, soldHeat / 1000.0, price);
		assertClose(dynamicCosts,
			r.costResult.dynamicTotal.consumptionCosts);
		assertClose(staticHeatCosts(project, soldHeat / 1000.0, price),
			r.costResult.staticTotal.consumptionCosts);

		var item = itemOf(r, producer);
		assertNotNull(item);
		assertClose(dynamicCosts, item.demandRelatedCosts());

		// the used heat and the heat generation costs only consider the heat
		// that is sold to the network
		double bufferLoss = Stats.sum(r.energyResult.bufferLoss);
		double usedHeat = (r.energyResult.totalProducedHeat
			- r.energyResult.heatNetLoss
			- bufferLoss
			- fermenterHeat) / 1000.0;
		assertTrue(usedHeat > 0);
		var stat = r.costResult.staticTotal;
		assertClose((stat.totalAnnualCosts - stat.revenuesElectricity) / usedHeat,
			stat.heatGenerationCosts);
		assertClose(usedHeat * project.costSettings.heatRevenues,
			stat.revenuesHeat);
	}

	@Test
	public void usesTheHeatPriceOfTheProducer() {
		var project = project();
		var producer = biogasProducer(project);
		producer.biogasHeatPrice = 42.0;
		var entry = new BiogasInvestmentEntry();
		entry.costs = new ProductCosts();
		entry.costs.investment = 5_000_000;
		producer.biogasPlant.investments.add(entry);

		var r = ProjectResult.calculate(project);

		double heat = Math.max(0, r.energyResult.totalHeat(producer)
			- r.energyResult.fermenterHeatOf(producer)) / 1000.0;
		assertTrue(heat > 0);
		assertClose(dynamicHeatCosts(project, heat, 42.0),
			r.costResult.dynamicTotal.consumptionCosts);
		assertClose(staticHeatCosts(project, heat, 42.0),
			r.costResult.staticTotal.consumptionCosts);
	}

	@Test
	public void appliesTheHeatPriceChangeFactor() {
		var project = project();
		project.costSettings.interestRate = 4.0;
		project.costSettings.heatRevenuesFactor = 1.02;
		var producer = biogasProducer(project);
		var entry = new BiogasInvestmentEntry();
		entry.costs = new ProductCosts();
		entry.costs.investment = 5_000_000;
		producer.biogasPlant.investments.add(entry);

		var r = ProjectResult.calculate(project);

		var biogasResult = r.biogasResults.get(producer);
		assertNotNull(biogasResult);
		double price = biogasResult.getEffectiveHeatPrice(null);
		double heat = Math.max(0, r.energyResult.totalHeat(producer)
			- r.energyResult.fermenterHeatOf(producer)) / 1000.0;
		assertTrue(heat > 0);

		double dynamicCosts = dynamicHeatCosts(project, heat, price);
		double staticCosts = staticHeatCosts(project, heat, price);
		assertClose(dynamicCosts,
			r.costResult.dynamicTotal.consumptionCosts);
		assertClose(staticCosts,
			r.costResult.staticTotal.consumptionCosts);
		assertTrue(dynamicCosts > staticCosts);
	}

	/// The static annuity of the heat purchase for the given heat in MWh.
	private static double staticHeatCosts(
		Project project, double heat, double price
	) {
		return Annuity.ofYearlyCosts(
			project.duration,
			heat * price,
			project.costSettings.interestRate,
			1.0);
	}

	/// The dynamic annuity of the heat purchase for the given heat in MWh.
	private static double dynamicHeatCosts(
		Project project, double heat, double price
	) {
		return Annuity.ofYearlyCosts(
			project.duration,
			heat * price,
			project.costSettings.interestRate,
			project.costSettings.heatRevenuesFactor);
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
		// the heat purchase is annuitized with this factor
		project.costSettings.heatRevenuesFactor = 1.0;

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
