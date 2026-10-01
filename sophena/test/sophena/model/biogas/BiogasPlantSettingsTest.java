package sophena.model.biogas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

/// Tests the default values of the biogas plant settings, in particular the
/// selection of the market value.
public class BiogasPlantSettingsTest {

	@Test
	public void usesFixedRemunerationWhenThereIsNoMarketValue() {
		var settings = BiogasPlantSettings.createDefault(null, List.of());
		assertTrue(settings.isFixedRemuneration);
		assertNull(settings.marketValue);
	}

	@Test
	public void usesFixedRemunerationWhenTheMarketValuesAreNull() {
		var settings = BiogasPlantSettings.createDefault(null, null);
		assertTrue(settings.isFixedRemuneration);
		assertNull(settings.marketValue);
	}

	@Test
	public void selectsTheMarketValueThatComesFirstWhenSortedDescending() {
		var value2022 = market("Marktwerte 2022");
		var value2025 = market("Marktwerte 2025");
		var value2024 = market("Marktwerte 2024");

		var settings = BiogasPlantSettings.createDefault(
			null, List.of(value2022, value2025, value2024));

		assertFalse(settings.isFixedRemuneration);
		assertSame(value2025, settings.marketValue);
	}

	@Test
	public void defaultFundingValues() {
		var settings = BiogasPlantSettings.createDefault(null, List.of());
		assertEquals(0.0, settings.annualFunding, 1e-16);
		assertEquals(0.0, settings.feedInTariff, 1e-16);
		assertEquals(18.0, settings.marketPremiumValue, 1e-16);
		assertTrue(settings.useAnnualMarketValue);
		assertEquals(0.0, settings.directMarketerShare, 1e-16);
		assertEquals(MarketPriceLimit.NONE, settings.marketPriceLimit);
	}

	@Test
	public void copyKeepsTheFundingSettings() {
		var settings = BiogasPlantSettings.createDefault(
			null, List.of(market("Marktwerte 2025")));
		settings.annualFunding = 1500.0;
		settings.isFixedRemuneration = true;
		settings.feedInTariff = 12.5;
		settings.marketPremiumValue = 19.3;
		settings.useAnnualMarketValue = false;
		settings.directMarketerShare = 20.0;
		settings.marketPriceLimit = MarketPriceLimit.BELOW_TWO;

		var copy = settings.copy();

		assertEquals(1500.0, copy.annualFunding, 1e-16);
		assertTrue(copy.isFixedRemuneration);
		assertEquals(12.5, copy.feedInTariff, 1e-16);
		assertEquals(19.3, copy.marketPremiumValue, 1e-16);
		assertSame(settings.marketValue, copy.marketValue);
		assertFalse(copy.useAnnualMarketValue);
		assertEquals(20.0, copy.directMarketerShare, 1e-16);
		assertEquals(MarketPriceLimit.BELOW_TWO, copy.marketPriceLimit);
	}

	private static ElectricityMarketValue market(String name) {
		var value = new ElectricityMarketValue();
		value.name = name;
		return value;
	}
}
