package sophena.model.biogas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.After;
import org.junit.Test;

import sophena.Tests;
import sophena.db.Database;

public class BiogasPlantSettingsTest {

	private final Database db = Tests.getDb();

	@After
	public void tearDown() {
		for (var mv : db.getAll(ElectricityMarketValue.class)) {
			db.delete(mv);
		}
	}

	@Test
	public void testDefault() {
		var settings = BiogasPlantSettings.createDefault();
		assertTrue(settings.isFixedRemuneration);
		assertNull(settings.marketValue);
	}

	@Test
	public void testMarketValueSelection() {
		market("Marktwerte 2022");
		var expected = market("Marktwerte 2025");
		market("Marktwerte 2024");

		var settings = BiogasPlantSettings.createDefault(db);

		assertFalse(settings.isFixedRemuneration);
		assertEquals(expected, settings.marketValue);
	}

	@Test
	public void defaultFundingValues() {
		var settings = BiogasPlantSettings.createDefault();
		assertEquals(0.0, settings.annualFunding, 1e-16);
		assertEquals(0.0, settings.feedInTariff, 1e-16);
		assertEquals(18.0, settings.marketPremiumValue, 1e-16);
		assertTrue(settings.useAnnualMarketValue);
		assertEquals(0.0, settings.directMarketerShare, 1e-16);
		assertEquals(MarketPriceLimit.NONE, settings.marketPriceLimit);
	}

	@Test
	public void copyKeepsTheFundingSettings() {
		market("Marktwerte 2025");
		var settings = BiogasPlantSettings.createDefault();
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

	private ElectricityMarketValue market(String name) {
		var value = new ElectricityMarketValue();
		value.id = UUID.randomUUID().toString();
		value.name = name;
		return db.insert(value);
	}
}
