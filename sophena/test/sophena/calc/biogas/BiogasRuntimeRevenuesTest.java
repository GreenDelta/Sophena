package sophena.calc.biogas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.UUID;

import org.junit.Test;

import sophena.calc.biogas.costs.BiogasRuntimeRevenues;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.BiogasPlantSettings;
import sophena.model.biogas.ElectricityMarketValue;
import sophena.model.biogas.MarketPriceLimit;

/// Tests the revenue calculation of a biogas plant.
public class BiogasRuntimeRevenuesTest {

	/// The electric power of the test plant under full load in kW.
	private static final double POWER = 500;

	@Test
	public void fixedRemunerationPaysTheTariff() {
		var plant = base();
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var r = BiogasRuntimeRevenues.calculate(plant, result).orElseThrow();

		double feedIn = expectedFeedIn(result.runFlags(), 30, false);
		assertEquals(feedIn, r.feedIn(), 1e-6);
		assertEquals(0.12 * feedIn, r.fundingSum(), 1e-6);
		assertEquals(0.10 * feedIn, r.revenuesSum(), 1e-6);
		assertEquals(0, r.additionalRevenues(), 1e-9);
		assertEquals(0, r.directMarketerPayment(), 1e-9);
		assertEquals(r.fundingSum() + r.revenuesSum(),
			r.totalRevenues(), 1e-6);
	}

	@Test
	public void fullFeedInDoesNotSubtractTheInternalDemand() {
		var plant = base();
		plant.settings.isFullFeedIn = true;
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var r = BiogasRuntimeRevenues.calculate(plant, result).orElseThrow();

		double expected = expectedFeedIn(result.runFlags(), 30, true);
		assertEquals(expected, r.feedIn(), 1e-6);

		var surplus = base();
		var surplusResult = BiogasRuntimeResult.calculate(surplus).orElseThrow();
		var r2 = BiogasRuntimeRevenues.calculate(surplus, surplusResult)
			.orElseThrow();
		assertTrue(r.feedIn() > r2.feedIn());
	}

	@Test
	public void marketPremiumUsesTheValueToBeApplied() {
		var plant = base();
		plant.settings.isFixedRemuneration = false;
		plant.settings.marketValue = marketValue(10, 10);
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var r = BiogasRuntimeRevenues.calculate(plant, result).orElseThrow();

		double feedIn = expectedFeedIn(result.runFlags(), 30, false);
		// premium = 18 - 10 = 8 ct/kWh
		assertEquals(0.08 * feedIn, r.fundingSum(), 1e-6);
		// the monthly value equals the annual value here
		assertEquals(0, r.additionalRevenues(), 1e-9);
	}

	@Test
	public void marketPremiumUsesTheMonthlyValue() {
		var plant = base();
		plant.settings.isFixedRemuneration = false;
		plant.settings.marketValue = marketValue(10, 5);
		plant.settings.useAnnualMarketValue = false;
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var r = BiogasRuntimeRevenues.calculate(plant, result).orElseThrow();

		double feedIn = expectedFeedIn(result.runFlags(), 30, false);
		// premium = 18 - 5 = 13 ct/kWh
		assertEquals(0.13 * feedIn, r.fundingSum(), 1e-6);
		assertEquals(0.05 * feedIn, r.additionalRevenues(), 1e-6);
	}

	@Test
	public void marketPremiumIsNeverNegative() {
		var plant = base();
		plant.settings.isFixedRemuneration = false;
		plant.settings.marketPremiumValue = 8;
		plant.settings.marketValue = marketValue(10, 10);
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var r = BiogasRuntimeRevenues.calculate(plant, result).orElseThrow();
		assertEquals(0, r.fundingSum(), 1e-9);
	}

	@Test
	public void additionalRevenuesAndDirectMarketerShare() {
		var plant = base();
		plant.settings.isFixedRemuneration = false;
		plant.settings.marketValue = marketValue(10, 5);
		plant.settings.useAnnualMarketValue = true;
		plant.settings.directMarketerShare = 20;
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var r = BiogasRuntimeRevenues.calculate(plant, result).orElseThrow();

		double feedIn = expectedFeedIn(result.runFlags(), 30, false);
		// additional revenues = feedIn * (10 - 5) / 100; the monthly value is
		// always used here, also when the annual value is selected
		assertEquals(0.05 * feedIn, r.additionalRevenues(), 1e-6);
		assertEquals(0.20 * r.additionalRevenues(),
			r.directMarketerPayment(), 1e-6);
		assertEquals(r.fundingSum() + r.revenuesSum() - r.directMarketerPayment(),
			r.totalRevenues(), 1e-6);
	}

	@Test
	public void marketPriceLimitBlocksTheFunding() {
		// all hours have a price of 1 ct/kWh
		var plant = TestPlant.of(1600, 4, h -> 1);
		plant.settings = settings();
		plant.settings.isFixedRemuneration = false;
		plant.settings.marketValue = marketValue(10, 10);

		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();

		plant.settings.marketPriceLimit = MarketPriceLimit.NONE;
		var without = BiogasRuntimeRevenues.calculate(plant, result)
			.orElseThrow();
		assertTrue(without.fundingSum() > 0);

		plant.settings.marketPriceLimit = MarketPriceLimit.BELOW_TWO;
		var with = BiogasRuntimeRevenues.calculate(plant, result).orElseThrow();
		assertEquals(0, with.fundingSum(), 1e-9);

		// the price limit does not affect the exchange revenues
		assertEquals(without.revenuesSum(), with.revenuesSum(), 1e-6);
	}

	@Test
	public void skipHourBlocksFundingAndRevenues() {
		var plant = base();
		plant.settings.isFixedRemuneration = false;
		plant.settings.marketValue = marketValue(10, 10);
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();

		int hour = firstRunHour(result.runFlags());
		assertTrue("the plant must run at some hour", hour >= 0);

		// block the feed-in of a run hour after the run flags were created;
		// this excluded hour gets neither funding nor exchange revenues
		var allowed = new boolean[Stats.HOURS];
		Arrays.fill(allowed, true);
		allowed[hour] = false;
		plant.electricityPrices.feedInAllowed = allowed;

		var r = BiogasRuntimeRevenues.calculate(plant, result).orElseThrow();
		assertEquals(0, r.funding()[hour], 1e-12);
		assertEquals(0, r.revenues()[hour], 1e-12);
	}

	@Test
	public void eligibleQuarterHoursLimitTheMarketPremium() {
		var plant = base();
		plant.settings.isFixedRemuneration = false;
		plant.settings.marketValue = marketValue(10, 10);
		plant.settings.eligibleQuarterHours = 4;
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var r = BiogasRuntimeRevenues.calculate(plant, result).orElseThrow();

		// the budget of 4 quarter hours funds exactly one full-load hour
		int funded = 0;
		for (double f : r.funding()) {
			if (f > 0)
				funded++;
		}
		assertEquals(1, funded);
	}

	@Test
	public void eligibleQuarterHoursDoNotLimitTheFixedRemuneration() {
		var plant = base();
		plant.settings.eligibleQuarterHours = 0;
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var r = BiogasRuntimeRevenues.calculate(plant, result).orElseThrow();
		assertTrue(r.fundingSum() > 0);
	}

	@Test
	public void quarterHourKpis() {
		var plant = TestPlant.of(1600, 4, h -> h < 100 ? -5 : 10);
		plant.settings = settings();
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var r = BiogasRuntimeRevenues.calculate(plant, result).orElseThrow();

		var flags = result.runFlags();
		double runs = countRuns(flags);
		double ramps = countRampUnits(flags);

		assertEquals(runs * 4, r.quarterHoursAbove85(), 1e-9);
		assertEquals(runs * 4 + ramps, r.totalQuarterHours(), 1e-9);

		double belowZero = 0;
		double belowTwo = 0;
		for (int h = 0; h < Stats.HOURS; h++) {
			if (!flags[h])
				continue;
			double price = plant.electricityPrices.values[h];
			if (price < 0)
				belowZero += 4;
			if (price <= 2)
				belowTwo += 4;
		}
		assertEquals(belowZero, r.quarterHoursBelowZero(), 1e-9);
		assertEquals(belowTwo, r.quarterHoursBelowTwo(), 1e-9);
	}

	@Test
	public void errors() {
		var plant = base();
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();

		assertTrue(BiogasRuntimeRevenues.calculate(null, result).isError());
		assertTrue(BiogasRuntimeRevenues.calculate(plant, null).isError());

		var noPrices = base();
		noPrices.electricityPrices = null;
		assertTrue(BiogasRuntimeRevenues.calculate(noPrices, result).isError());

		var noSettings = base();
		noSettings.settings = null;
		assertTrue(BiogasRuntimeRevenues.calculate(noSettings, result).isError());

		var noMarketValue = base();
		noMarketValue.settings.isFixedRemuneration = false;
		noMarketValue.settings.marketValue = null;
		assertTrue(BiogasRuntimeRevenues
			.calculate(noMarketValue, result)
			.isError());
	}

	/// A plant with a fixed remuneration of 12 ct/kWh, an internal power demand
	/// of 30 kW and a surplus feed-in.
	private static BiogasPlant base() {
		var plant = TestPlant.of(1600, 4);
		plant.settings = settings();
		plant.settings.isFixedRemuneration = true;
		plant.settings.feedInTariff = 12;
		return plant;
	}

	private static BiogasPlantSettings settings() {
		var settings = BiogasPlantSettings.createDefault(null);
		settings.avgPowerDemand = 30;
		settings.isFullFeedIn = false;
		settings.directMarketerShare = 0;
		settings.eligibleQuarterHours = 35040;
		settings.isFixedRemuneration = true;
		settings.feedInTariff = 12;
		settings.marketPremiumValue = 18;
		return settings;
	}

	private static ElectricityMarketValue marketValue(
		double annual, double monthly
	) {
		var value = new ElectricityMarketValue();
		value.id = UUID.randomUUID().toString();
		value.name = "Marktwert";
		value.value = annual;
		value.monthlyValues = new double[12];
		Arrays.fill(value.monthlyValues, monthly);
		return value;
	}

	/// The expected fed-in electricity in kWh: the full-load hours contribute
	/// the electric power (minus the internal demand in the surplus mode) and
	/// each ramp contributes 1/8 of the power.
	private static double expectedFeedIn(
		boolean[] flags, double avgDemand, boolean fullFeedIn
	) {
		double sum = 0;
		for (int h = 0; h < flags.length; h++) {
			if (flags[h]) {
				sum += fullFeedIn
					? POWER
					: Math.max(0, POWER - avgDemand);
			}
		}
		return sum + countRampUnits(flags) * POWER / 8.0;
	}

	private static double countRuns(boolean[] flags) {
		double n = 0;
		for (boolean flag : flags) {
			if (flag)
				n++;
		}
		return n;
	}

	/// Counts the ramp units: an off hour that is before or after a run hour
	/// has one ramp, an hour between two blocks has two.
	private static double countRampUnits(boolean[] flags) {
		double n = 0;
		for (int h = 0; h < flags.length; h++) {
			if (flags[h])
				continue;
			if (h + 1 < flags.length && flags[h + 1])
				n++;
			if (h > 0 && flags[h - 1])
				n++;
		}
		return n;
	}

	private static int firstRunHour(boolean[] flags) {
		for (int h = 0; h < flags.length; h++) {
			if (flags[h])
				return h;
		}
		return -1;
	}
}
