package sophena.model.biogas;

import org.junit.Assert;
import org.junit.Test;

public class ElectricityMarketValueCloneTest {

	@Test
	public void testClone() {
		var origin = new ElectricityMarketValue();
		origin.id = "id";
		origin.name = "Marktwerte 2022";
		origin.description = "a description";
		origin.isProtected = true;
		origin.year = 2022;
		origin.value = 23.545;
		origin.source = "https://www.netztransparenz.de";
		origin.monthlyValues = new double[12];
		for (int month = 1; month <= 12; month++) {
			origin.monthlyValues[month - 1] = month;
		}

		var clone = origin.copy();

		Assert.assertNotEquals(origin.id, clone.id);
		Assert.assertEquals(origin.name, clone.name);
		Assert.assertEquals(origin.description, clone.description);
		Assert.assertEquals(origin.isProtected, clone.isProtected);
		Assert.assertEquals(origin.year, clone.year);
		Assert.assertEquals(origin.value, clone.value, 1e-16);
		Assert.assertEquals(origin.source, clone.source);

		// the monthly values are copied into a new array
		Assert.assertNotNull(clone.monthlyValues);
		Assert.assertNotSame(origin.monthlyValues, clone.monthlyValues);
		Assert.assertArrayEquals(origin.monthlyValues, clone.monthlyValues, 1e-16);
	}

	@Test
	public void testMonthlyValueOf() {
		var value = new ElectricityMarketValue();
		Assert.assertEquals(0, value.monthlyValueOf(1), 1e-16);

		value.monthlyValues = new double[12];
		for (int month = 1; month <= 12; month++) {
			value.monthlyValues[month - 1] = month * 1.5;
		}
		Assert.assertEquals(1.5, value.monthlyValueOf(1), 1e-16);
		Assert.assertEquals(18.0, value.monthlyValueOf(12), 1e-16);
		Assert.assertEquals(0, value.monthlyValueOf(0), 1e-16);
		Assert.assertEquals(0, value.monthlyValueOf(13), 1e-16);
	}

	@Test
	public void testCloneWithoutMonthlyValues() {
		var value = new ElectricityMarketValue();
		value.monthlyValues = null;
		Assert.assertNull(value.copy().monthlyValues);
	}
}
