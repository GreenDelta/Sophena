package sophena.calc.biogas.biomethane;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import sophena.calc.costs.Annuity;
import sophena.model.AnnualCostEntry;
import sophena.model.Boiler;
import sophena.model.ProductCosts;
import sophena.model.Stats;
import sophena.model.biogas.BiogasInvestmentEntry;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.BiogasPlantBoiler;
import sophena.model.biogas.BiogasPlantSettings;
import sophena.model.biogas.Substrate;
import sophena.model.biogas.SubstrateProfile;

/// Tests the biomethane calculation with a plant that has the substrates of the
/// example calculation: 10.000 t cattle slurry, 3.000 t cattle manure,
/// 2.000 t maize silage, and 2.000 t clover grass per year.
public class BiomethaneCalculatorTest {

	private BiogasPlant plant;

	@Before
	public void setUp() {
		plant = new BiogasPlant();
		plant.duration = 15;
		plant.settings = BiogasPlantSettings.createDefault();
		plant.substrateProfiles.add(
			profile("Rindergülle", 10_000, 5, 8.9, 78.9, 456, 56.0, -280.8));
		plant.substrateProfiles.add(
			profile("Rindermist", 3_000, 5, 22.9, 82.2, 350, 58.0, -280.8));
		plant.substrateProfiles.add(
			profile("Maissilage", 2_000, 50, 35.1, 96.5, 658, 54.3, 126));
		plant.substrateProfiles.add(
			profile("Kleegras", 2_000, 40, 35.6, 88.9, 564, 57.1, 126));

		// the CHP must not be part of the biomethane option
		var boiler = new BiogasPlantBoiler();
		boiler.boiler = new Boiler();
		boiler.costs = costs(500_000, 10, 5);
		plant.boilers.add(boiler);
	}

	@Test
	public void gasVolumesFromSubstrates() {
		var r = calculate(BiomethaneInput.of(plant));

		double biogas = 10_000 * 0.089 * 0.789 * 456
			+ 3_000 * 0.229 * 0.822 * 350
			+ 2_000 * 0.351 * 0.965 * 658
			+ 2_000 * 0.356 * 0.889 * 564;
		assertEquals(biogas, r.biogasVolume, 1e-3);
		assertEquals(1_320_601, r.biogasVolume, 1);
		assertEquals(739_838.5, r.methaneVolume, 1);
		assertEquals(r.methaneVolume * 9.97, r.rawGasEnergy, 1e-6);
		assertEquals(0.5602, r.methaneContent(), 1e-4);
	}

	@Test
	public void capacityAndInvestment() {
		var input = BiomethaneInput.of(plant);
		var r = calculate(input);
		// 1.320.601 m³ / 7.500 h = 176,1 m³/h
		assertEquals(r.biogasVolume / 7_500, r.capacity, 1e-9);
		assertEquals(176.08, r.capacity, 0.01);
		assertEquals(r.capacity * 7_500, r.upgradingInvestment, 1e-6);

		input.fullLoadHours = 8000;
		input.specificInvestment = 8000;
		r = calculate(input);
		assertEquals(r.biogasVolume / 8000, r.capacity, 1e-9);
		assertEquals(r.capacity * 8000, r.upgradingInvestment, 1e-6);

		// at most all hours of the year
		input.fullLoadHours = 10_000;
		assertEquals(r.biogasVolume / 8760, calculate(input).capacity, 1e-9);
	}


	@Test
	public void manureClassificationAndPrices() {
		assertTrue(BiomethaneCalculator.isManure(-280.8));
		assertTrue(BiomethaneCalculator.isManure(-200.1));
		assertFalse(BiomethaneCalculator.isManure(-200));
		assertFalse(BiomethaneCalculator.isManure(126));

		var input = BiomethaneInput.of(plant);
		assertEquals(0.12, input.manurePrice, 1e-10);
		assertEquals(0.08, input.nonManurePrice, 1e-10);
		var r = calculate(input);

		assertEquals(4, r.substrates.size());
		assertTrue(r.substrates.get(0).isManure());
		assertTrue(r.substrates.get(1).isManure());
		assertFalse(r.substrates.get(2).isManure());
		assertFalse(r.substrates.get(3).isManure());
		double shares = r.substrates.stream()
			.mapToDouble(BiomethaneResult.SubstrateShare::share)
			.sum();
		assertEquals(1, shares, 1e-10);

		// 39,73 % manure x 12 ct + 60,27 % x 8 ct = 9,59 ct/kWh
		assertEquals(0.0959, r.mixedPrice(), 1e-4);
		assertEquals(0.99 * r.rawGasEnergy, r.soldEnergy(), 1e-6);
		assertEquals(r.soldEnergy() * r.mixedPrice(),
			r.staticTotal.totalRevenues, 1e-6);
	}

	@Test
	public void staticCosts() {
		var input = BiomethaneInput.of(plant);
		input.interestRate = 4;
		input.duration = 15;
		input.grid.lifetime = 15;
		var r = calculate(input);
		var s = r.staticTotal;

		// no plant investments; upgrading and grid connection
		double investment = r.upgradingInvestment + 250_000;
		assertEquals(investment, s.investments, 1e-6);
		assertEquals(investment * Annuity.factor(15, 4), s.capitalCosts, 1e-6);

		// the static annuity of yearly costs is the first year value
		double substrates = 10_000 * 5 + 3_000 * 5 + 2_000 * 50 + 2_000 * 40;
		double electricity = 30 * 8760 * 0.30;
		double running = 28 * r.rawGasEnergy / 1000;
		assertEquals(substrates + electricity + running,
			s.consumptionCosts, 1e-6);
		assertEquals(0, s.operationCosts, 1e-6);
		// insurance 0,5 % + other charges 0,25 %
		assertEquals(investment * 0.0075, s.otherAnnualCosts, 1e-6);

		assertEquals(s.capitalCosts + s.consumptionCosts
			+ s.operationCosts + s.otherAnnualCosts, s.totalAnnualCosts, 1e-6);
		assertEquals(s.totalRevenues - s.totalAnnualCosts,
			s.annualSurplus, 1e-6);
	}

	@Test
	public void operationAndOtherCosts() {
		var input = BiomethaneInput.of(plant);
		input.upgrading.repair = 2;
		input.upgrading.maintenance = 1;
		input.upgrading.operation = 100;
		input.hourlyWage = 30;
		var entry = new AnnualCostEntry();
		entry.value = 5_000;
		plant.otherAnnualCosts.add(entry);

		var s = calculate(input).staticTotal;
		double upgrading = calculate(input).upgradingInvestment;
		assertEquals(0.03 * upgrading + 100 * 30, s.operationCosts, 1e-6);
		assertEquals(0.0075 * (upgrading + 250_000) + 5_000,
			s.otherAnnualCosts, 1e-6);
	}

	@Test
	public void plantInvestmentsWithoutChp() {
		var fermenter = new BiogasInvestmentEntry();
		fermenter.name = "Fermenter";
		fermenter.costs = costs(1_000_000, 20, 1);
		plant.investments.add(fermenter);

		var input = BiomethaneInput.of(plant);
		assertEquals(1, input.plantInvestments.size());
		double upgrading = calculate(input).upgradingInvestment;
		var with = calculate(input).staticTotal;
		assertEquals(1_000_000 + upgrading + 250_000, with.investments, 1e-6);

		input.plantInvestments.getFirst().included = false;
		var without = calculate(input).staticTotal;
		assertEquals(upgrading + 250_000, without.investments, 1e-6);
		assertTrue(with.capitalCosts > without.capitalCosts);
		assertEquals(10_000, with.operationCosts - without.operationCosts, 1e-6);
	}

	@Test
	public void fundingReducesCapitalCosts() {
		var input = BiomethaneInput.of(plant);
		var without = calculate(input).staticTotal;
		input.funding = 100_000;
		var with = calculate(input).staticTotal;
		assertEquals(100_000, with.investmentFunding, 1e-9);
		assertEquals(100_000 * Annuity.factor(input.duration, input.interestRate),
			without.capitalCosts - with.capitalCosts, 1e-6);
	}

	@Test
	public void dynamicEqualsStaticWithoutPriceChanges() {
		var input = BiomethaneInput.of(plant);
		input.investmentFactor = 1;
		input.bioFuelFactor = 1;
		input.electricityFactor = 1;
		input.operationFactor = 1;
		input.maintenanceFactor = 1;
		input.runningCostsFactor = 1;
		input.revenuesFactor = 1;
		var r = calculate(input);
		assertEquals(r.staticTotal.totalAnnualCosts,
			r.dynamicTotal.totalAnnualCosts, 1e-6);
		assertEquals(r.staticTotal.totalRevenues,
			r.dynamicTotal.totalRevenues, 1e-6);

		input.revenuesFactor = 1.02;
		input.runningCostsFactor = 1.03;
		r = calculate(input);
		assertTrue(r.dynamicTotal.totalRevenues > r.staticTotal.totalRevenues);
		assertTrue(
			r.dynamicTotal.consumptionCosts > r.staticTotal.consumptionCosts);
	}

	private BiomethaneResult calculate(BiomethaneInput input) {
		return BiomethaneCalculator.calculate(plant, input);
	}

	private static SubstrateProfile profile(
		String name, double mass, double costs, double dm, double odm,
		double biogas, double methane, double co2
	) {
		var sub = new Substrate();
		sub.name = name;
		sub.dryMatter = dm;
		sub.organicDryMatter = odm;
		sub.biogasProduction = biogas;
		sub.methaneContent = methane;
		sub.co2Emissions = co2;
		var p = new SubstrateProfile();
		p.substrate = sub;
		p.annualMass = mass;
		p.substrateCosts = costs;
		p.hourlyValues = new double[Stats.HOURS];
		Arrays.fill(p.hourlyValues, mass / Stats.HOURS);
		return p;
	}

	private static ProductCosts costs(
		double investment, int duration, double maintenance
	) {
		var c = new ProductCosts();
		c.investment = investment;
		c.duration = duration;
		c.maintenance = maintenance;
		return c;
	}
}
