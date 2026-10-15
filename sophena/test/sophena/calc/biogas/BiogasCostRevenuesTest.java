package sophena.calc.biogas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.UUID;
import java.util.function.IntToDoubleFunction;

import org.junit.Test;

import sophena.calc.biogas.costs.BiogasCostCalculator;
import sophena.calc.biogas.costs.BiogasCostResult;
import sophena.calc.biogas.costs.BiogasRuntimeRevenues;
import sophena.calc.biogas.runtime.BiogasRuntimeResult;
import sophena.model.Stats;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.BiogasPlantSettings;
import sophena.model.biogas.ElectricityMarketValue;

/// Tests the electricity revenues in the cost result of a biogas plant. The
/// interest rate of the test plants is 0 %, so that the annuity of a yearly
/// value equals the first-year value.
public class BiogasCostRevenuesTest {

	@Test
	public void priceChangeOnlyAffectsTheAdditionalRevenues() {
		var withFactor1 = plant(h -> 10);
		withFactor1.settings.electricityRevenuesFactor = 1.0;
		var withFactor15 = plant(h -> 10);
		withFactor15.settings.electricityRevenuesFactor = 1.5;

		var a = costsOf(withFactor1);
		var b = costsOf(withFactor15);

		// the feed-in funding and the reference market revenues do not have a
		// price change factor
		assertEquals(a.dynamicTotal.feedInFundingRevenues,
			b.dynamicTotal.feedInFundingRevenues, 1e-6);
		assertEquals(a.dynamicTotal.referenceMarketRevenues,
			b.dynamicTotal.referenceMarketRevenues, 1e-6);
		assertEquals(a.dynamicTotal.feedInFundingRevenues,
			a.staticTotal.feedInFundingRevenues, 1e-6);
		assertEquals(a.dynamicTotal.referenceMarketRevenues,
			a.staticTotal.referenceMarketRevenues, 1e-6);

		// the additional revenues are the only part with a price change factor
		assertTrue(b.dynamicTotal.additionalRevenues
			> a.dynamicTotal.additionalRevenues);
		assertEquals(a.staticTotal.additionalRevenues,
			b.staticTotal.additionalRevenues, 1e-6);
	}

	@Test
	public void additionalRevenuesNetTheNegativeDeviations() {
		// the price is below the market value in the first month and above it
		// afterwards; the additional revenues are the difference of the two
		// sums and not the sum of the positive per-hour differences
		var plant = plant(h -> h < 744 ? 2 : 14);
		var runtime = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var revenues = BiogasRuntimeRevenues.calculate(plant, runtime)
			.orElseThrow();
		var costs = new BiogasCostCalculator(plant, runtime, revenues)
			.calculate();

		double spot = revenues.spotMarketRevenuesSum();
		double reference = revenues.marketRevenuesSum();
		assertTrue(reference > 0);

		var feedIn = TestPlant.feedInProfile(plant, runtime.runFlags());
		double clamped = 0;
		for (int h = 0; h < Stats.HOURS; h++) {
			double diff = revenues.spotMarketRevenues()[h]
				- feedIn[h] * 8 / 100.0;
			clamped += Math.max(0, diff);
		}
		assertTrue("the plant must also run below the market value",
			spot - reference < clamped);
		assertEquals(spot - reference,
			costs.dynamicTotal.additionalRevenues, 1e-6);
		assertEquals(spot - reference,
			costs.staticTotal.additionalRevenues, 1e-6);
	}

	@Test
	public void directMarketerPaymentIsTheShareOfTheAdditionalRevenues() {
		var plant = plant(h -> 14);
		plant.settings.directMarketerShare = 20;
		var runtime = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var revenues = BiogasRuntimeRevenues.calculate(plant, runtime)
			.orElseThrow();
		var costs = new BiogasCostCalculator(plant, runtime, revenues)
			.calculate();

		double additional = revenues.spotMarketRevenuesSum()
			- revenues.marketRevenuesSum();
		assertTrue(additional > 0);
		assertEquals(0.20 * additional,
			costs.dynamicTotal.directMarketerPayment, 1e-6);
		assertEquals(0.20 * additional,
			costs.staticTotal.directMarketerPayment, 1e-6);

		// the payment reduces the electricity revenues
		assertEquals(revenues.feedInFundingSum()
			+ revenues.marketRevenuesSum()
			+ additional
			- costs.dynamicTotal.directMarketerPayment,
			costs.dynamicTotal.revenuesElectricity, 1e-6);
		assertEquals(costs.dynamicTotal.annualFunding
			+ costs.dynamicTotal.revenuesElectricity
			+ costs.dynamicTotal.revenuesHeat,
			costs.dynamicTotal.totalRevenues, 1e-6);
	}

	@Test
	public void directMarketerPaymentIsNeverNegative() {
		// the spot market price is always below the market value, so the
		// additional revenues are negative and the payment is zero
		var plant = plant(h -> h < 4000 ? 2 : 5);
		plant.settings.directMarketerShare = 50;
		var runtime = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var revenues = BiogasRuntimeRevenues.calculate(plant, runtime)
			.orElseThrow();
		var costs = new BiogasCostCalculator(plant, runtime, revenues)
			.calculate();

		double additional = revenues.spotMarketRevenuesSum()
			- revenues.marketRevenuesSum();
		assertTrue("the additional revenues must be negative", additional < 0);
		assertEquals(additional, costs.dynamicTotal.additionalRevenues, 1e-6);
		assertEquals(0, costs.dynamicTotal.directMarketerPayment, 1e-9);
		assertEquals(0, costs.staticTotal.directMarketerPayment, 1e-9);

		// the additional revenues are only reduced when they are positive
		assertEquals(revenues.feedInFundingSum()
			+ revenues.spotMarketRevenuesSum(),
			costs.dynamicTotal.revenuesElectricity, 1e-6);
	}

	/// A plant with a market premium of 18 ct/kWh, a monthly market value of
	/// 8 ct/kWh, no interest rate, and the given spot market prices.
	private static BiogasPlant plant(IntToDoubleFunction price) {
		var plant = TestPlant.of(1600, 4, price);
		plant.duration = 20;
		var settings = BiogasPlantSettings.createDefault(null);
		settings.avgPowerDemand = 30;
		settings.isFullFeedIn = false;
		settings.isFixedRemuneration = false;
		settings.marketPremiumValue = 18;
		settings.marketValue = marketValue(8);
		settings.directMarketerShare = 0;
		settings.electricityRevenuesFactor = 1.0;
		settings.interestRate = 0;
		settings.eligibleQuarterHours = 35040;
		plant.settings = settings;
		return plant;
	}

	private static ElectricityMarketValue marketValue(double value) {
		var mv = new ElectricityMarketValue();
		mv.id = UUID.randomUUID().toString();
		mv.name = "Marktwert";
		mv.value = value;
		mv.monthlyValues = new double[12];
		Arrays.fill(mv.monthlyValues, value);
		return mv;
	}

	private static BiogasCostResult costsOf(BiogasPlant plant) {
		var runtime = BiogasRuntimeResult.calculate(plant).orElseThrow();
		var revenues = BiogasRuntimeRevenues.calculate(plant, runtime)
			.orElseThrow();
		return new BiogasCostCalculator(plant, runtime, revenues).calculate();
	}
}
