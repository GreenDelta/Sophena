package sophena.calc.biogas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.UUID;

import org.junit.Test;

import sophena.calc.biogas.costs.BiogasRuntimeRevenues;
import sophena.calc.biogas.runtime.BiogasRuntimeResult;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.BiogasPlantSettings;
import sophena.model.biogas.ElectricityMarketValue;
import sophena.model.biogas.MarketPriceLimit;

/// Tests the revenue calculation of a biogas plant.
public class BiogasRuntimeRevenuesTest {

	/// The electric power of the test plant under full load in kW.
	private static final double POWER = 500;

	/// The cable and transformer losses of the test plants in kW (the default
	/// value of `BiogasPlantSettings#createDefault`).
	private static final double LOSSES = 7.35;

	@Test
	public void fixedRemunerationPaysTheTariff() {
		var plant = base();
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var r = BiogasRuntimeRevenues.calculate(plant, result).orElseThrow();

		double feedIn = expectedFeedIn(result.runFlags(), 30, LOSSES, false);
		assertEquals(feedIn, r.feedInElectricity(), 1e-6);
		assertEquals(0.12 * feedIn, r.feedInFundingSum(), 1e-6);
		assertEquals(0.10 * feedIn, r.spotMarketRevenuesSum(), 1e-6);
		assertEquals(0, r.additionalRevenues(), 1e-9);
		assertEquals(0, r.directMarketerPayment(), 1e-9);
		assertEquals(r.feedInFundingSum() + r.spotMarketRevenuesSum(),
			r.totalRevenues(), 1e-6);
	}

	@Test
	public void fullFeedInDoesNotSubtractTheInternalDemand() {
		var plant = base();
		plant.settings.isFullFeedIn = true;
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var r = BiogasRuntimeRevenues.calculate(plant, result).orElseThrow();

		double expected = expectedFeedIn(result.runFlags(), 30, LOSSES, true);
		assertEquals(expected, r.feedInElectricity(), 1e-6);

		var surplus = base();
		var surplusResult = BiogasRuntimeResult.calculate(surplus).orElseThrow();
		var r2 = BiogasRuntimeRevenues.calculate(surplus, surplusResult)
			.orElseThrow();
		assertTrue(r.feedInElectricity() > r2.feedInElectricity());
	}

	@Test
	public void transmissionLossesReduceTheFeedIn() {
		var noLosses = base();
		noLosses.settings.transmissionLosses = 0;
		var result = BiogasRuntimeResult.calculate(noLosses).orElseThrow();
		var without = BiogasRuntimeRevenues.calculate(noLosses, result)
			.orElseThrow();

		var withLosses = base();
		withLosses.settings.transmissionLosses = 20;
		var with20 = BiogasRuntimeRevenues.calculate(withLosses, result)
			.orElseThrow();

		assertTrue(without.feedInElectricity() > with20.feedInElectricity());
		assertEquals(
			expectedFeedIn(result.runFlags(), 30, 20, false),
			with20.feedInElectricity(), 1e-6);
	}

	@Test
	public void totalGeneratedHeatIsTheSumOfTheProducerProfile() {
		var plant = base();
		plant.boilers.getFirst().boiler.maxPower = 600;
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var r = BiogasRuntimeRevenues.calculate(plant, result).orElseThrow();

		assertTrue(r.totalGeneratedHeat() > 0);
		assertEquals(Stats.sum(result.producerProfile().maxPower),
			r.totalGeneratedHeat(), 1e-6);
	}

	@Test
	public void marketPremiumUsesTheValueToBeApplied() {
		var plant = base();
		plant.settings.isFixedRemuneration = false;
		plant.settings.marketValue = marketValue(10, 10);
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var r = BiogasRuntimeRevenues.calculate(plant, result).orElseThrow();

		double feedIn = expectedFeedIn(result.runFlags(), 30, LOSSES, false);
		// premium = 18 - 10 = 8 ct/kWh
		assertEquals(0.08 * feedIn, r.feedInFundingSum(), 1e-6);
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

		double feedIn = expectedFeedIn(result.runFlags(), 30, LOSSES, false);
		// premium = 18 - 5 = 13 ct/kWh
		assertEquals(0.13 * feedIn, r.feedInFundingSum(), 1e-6);
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
		assertEquals(0, r.feedInFundingSum(), 1e-9);
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

		double feedIn = expectedFeedIn(result.runFlags(), 30, LOSSES, false);
		// additional revenues = feedIn * (10 - 5) / 100; the monthly value is
		// always used here, also when the annual value is selected
		assertEquals(0.05 * feedIn, r.additionalRevenues(), 1e-6);
		assertEquals(0.20 * r.additionalRevenues(),
			r.directMarketerPayment(), 1e-6);
		assertEquals(r.feedInFundingSum() + r.spotMarketRevenuesSum() - r.directMarketerPayment(),
			r.totalRevenues(), 1e-6);
	}

	@Test
	public void additionalRevenuesNetTheNegativeDeviations() {
		// the price is below the market value in the first month and above it
		// afterwards; the additional revenues are the signed difference of the
		// two sums and not the sum of the positive per-hour differences
		var plant = TestPlant.of(1600, 4, h -> h < 744 ? 2 : 14);
		plant.settings = settings();
		plant.settings.isFixedRemuneration = false;
		plant.settings.marketValue = marketValue(8, 8);
		plant.settings.directMarketerShare = 20;
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var r = BiogasRuntimeRevenues.calculate(plant, result).orElseThrow();

		var feedIn = hourlyFeedIn(result.runFlags(), 30, LOSSES, false);
		double referenceSum = 0;
		double netted = 0;
		double clamped = 0;
		boolean below = false;
		boolean above = false;
		for (int h = 0; h < Stats.HOURS; h++) {
			double reference = feedIn[h] * 8 / 100.0;
			referenceSum += reference;
			double diff = r.spotMarketRevenues()[h] - reference;
			netted += diff;
			clamped += Math.max(0, diff);
			if (feedIn[h] > 0 && diff < 0)
				below = true;
			if (feedIn[h] > 0 && diff > 0)
				above = true;
		}
		assertTrue("the plant must run below the market value", below);
		assertTrue("the plant must run above the market value", above);
		assertTrue(netted < clamped);

		assertEquals(r.spotMarketRevenuesSum() - referenceSum,
			r.additionalRevenues(), 1e-6);
		assertEquals(netted, r.additionalRevenues(), 1e-6);
		assertEquals(Math.max(0, 0.20 * netted),
			r.directMarketerPayment(), 1e-6);
	}

	@Test
	public void directMarketerPaymentIsNeverNegative() {
		// the spot market price is always below the market value, so the
		// additional revenues are negative and the payment is zero
		var plant = TestPlant.of(1600, 4, h -> h < 4000 ? 2 : 5);
		plant.settings = settings();
		plant.settings.isFixedRemuneration = false;
		plant.settings.marketValue = marketValue(8, 8);
		plant.settings.directMarketerShare = 50;
		var result = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var r = BiogasRuntimeRevenues.calculate(plant, result).orElseThrow();

		double feedIn = expectedFeedIn(result.runFlags(), 30, LOSSES, false);
		assertTrue(feedIn > 0);
		double expected = r.spotMarketRevenuesSum() - 0.08 * feedIn;
		assertTrue("the additional revenues must be negative", expected < 0);
		assertEquals(expected, r.additionalRevenues(), 1e-6);
		assertEquals(0, r.directMarketerPayment(), 1e-9);
		assertEquals(r.feedInFundingSum() + r.spotMarketRevenuesSum(),
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
		assertTrue(without.feedInFundingSum() > 0);

		plant.settings.marketPriceLimit = MarketPriceLimit.BELOW_TWO;
		var with = BiogasRuntimeRevenues.calculate(plant, result).orElseThrow();
		assertEquals(0, with.feedInFundingSum(), 1e-9);

		// the price limit does not affect the exchange revenues
		assertEquals(without.spotMarketRevenuesSum(), with.spotMarketRevenuesSum(), 1e-6);
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
		assertEquals(0, r.feedInFunding()[hour], 1e-12);
		assertEquals(0, r.spotMarketRevenues()[hour], 1e-12);
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
		for (double f : r.feedInFunding()) {
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
		assertTrue(r.feedInFundingSum() > 0);
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

		var noSettings = base();
		noSettings.settings = null;
		assertTrue(BiogasRuntimeRevenues.calculate(noSettings, result).isError());
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
	/// each ramp contributes 1/8 of the power. The cable and transformer losses
	/// are subtracted from every hour that feeds in.
	private static double expectedFeedIn(
		boolean[] flags, double avgDemand, double losses, boolean fullFeedIn
	) {
		double sum = 0;
		for (int h = 0; h < flags.length; h++) {
			if (flags[h]) {
				double energy = fullFeedIn
					? POWER
					: POWER - avgDemand;
				sum += Math.max(0, energy - losses);
				continue;
			}
			int ramps = 0;
			if (h + 1 < flags.length && flags[h + 1])
				ramps++;
			if (h > 0 && flags[h - 1])
				ramps++;
			if (ramps > 0) {
				sum += Math.max(0, ramps * POWER / 8.0 - losses);
			}
		}
		return sum;
	}

	/// The fed-in electricity in each hour in kWh; this is the profile that
	/// `expectedFeedIn` sums up.
	private static double[] hourlyFeedIn(
		boolean[] flags, double avgDemand, double losses, boolean fullFeedIn
	) {
		var feedIn = new double[flags.length];
		for (int h = 0; h < flags.length; h++) {
			if (flags[h]) {
				double energy = fullFeedIn
					? POWER
					: POWER - avgDemand;
				feedIn[h] = Math.max(0, energy - losses);
				continue;
			}
			int ramps = 0;
			if (h + 1 < flags.length && flags[h + 1])
				ramps++;
			if (h > 0 && flags[h - 1])
				ramps++;
			if (ramps > 0) {
				feedIn[h] = Math.max(0, ramps * POWER / 8.0 - losses);
			}
		}
		return feedIn;
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
