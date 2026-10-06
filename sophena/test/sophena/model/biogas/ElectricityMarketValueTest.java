package sophena.model.biogas;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

import sophena.model.Stats;

public class ElectricityMarketValueTest {

	@Test
	public void testClone() {
		var origin = new ElectricityMarketValue();
		origin.id = "id";
		origin.name = "Marktwerte 2022";
		origin.description = "a description";
		origin.isProtected = true;
		origin.value = 23.545;
		origin.source = "https://www.netztransparenz.de";
		origin.monthlyValues = new double[12];
		for (int month = 1; month <= 12; month++) {
			origin.monthlyValues[month - 1] = month;
		}

		var clone = origin.copy();

		assertNotEquals(origin.id, clone.id);
		assertEquals(origin.name, clone.name);
		assertEquals(origin.description, clone.description);
		assertEquals(origin.isProtected, clone.isProtected);
		assertEquals(origin.value, clone.value, 1e-16);
		assertEquals(origin.source, clone.source);

		// the monthly values are copied into a new array
		assertNotNull(clone.monthlyValues);
		assertNotSame(origin.monthlyValues, clone.monthlyValues);
		assertArrayEquals(origin.monthlyValues, clone.monthlyValues, 1e-16);
	}

	@Test
	public void testMonthlyValueOf() {
		var value = new ElectricityMarketValue();
		assertEquals(0, value.monthlyValueOf(1), 1e-16);

		value.monthlyValues = new double[12];
		for (int month = 1; month <= 12; month++) {
			value.monthlyValues[month - 1] = month * 1.5;
		}
		assertEquals(1.5, value.monthlyValueOf(1), 1e-16);
		assertEquals(18.0, value.monthlyValueOf(12), 1e-16);
		assertEquals(0, value.monthlyValueOf(0), 1e-16);
		assertEquals(0, value.monthlyValueOf(13), 1e-16);
	}

	@Test
	public void testCloneWithoutMonthlyValues() {
		var value = new ElectricityMarketValue();
		value.monthlyValues = null;
		Assert.assertNull(value.copy().monthlyValues);
	}

	@Test
	public void testMonthlyValueOfHour() {
		var value = new ElectricityMarketValue();

		// without monthly values the annual value is used
		value.value = 7.5;
		assertEquals(7.5, value.monthlyValueOfHour(0), 1e-16);
		assertEquals(7.5, value.monthlyValueOfHour(5000), 1e-16);

		value.monthlyValues = new double[12];
		for (int month = 1; month <= 12; month++) {
			value.monthlyValues[month - 1] = month;
		}

		// each hour is mapped to the value of its month
		assertEquals(1, value.monthlyValueOfHour(0), 1e-16);     // 01.01. 00:00
		assertEquals(1, value.monthlyValueOfHour(743), 1e-16);   // 31.01. 23:00
		assertEquals(2, value.monthlyValueOfHour(744), 1e-16);   // 01.02. 00:00
		assertEquals(2, value.monthlyValueOfHour(1415), 1e-16);  // 28.02. 23:00
		assertEquals(3, value.monthlyValueOfHour(1416), 1e-16);  // 01.03. 00:00
		assertEquals(12, value.monthlyValueOfHour(8759), 1e-16); // 31.12. 23:00

		// out of range hours fall back to the first/last month
		assertEquals(1, value.monthlyValueOfHour(-1), 1e-16);
		assertEquals(12, value.monthlyValueOfHour(Stats.HOURS), 1e-16);
	}
}
