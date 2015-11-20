package sophena.util;

import org.junit.Assert;
import org.junit.Test;

import sophena.utils.Num;

public class NumTest {

	@Test
	public void testIntFormat() {
		Assert.assertEquals("10", Num.intStr(10));
		Assert.assertEquals("10", Num.intStr(10d));
		Assert.assertEquals("1.000", Num.intStr(1000));
		Assert.assertEquals("1.000", Num.intStr(1000d));
		Assert.assertEquals("1.000.000", Num.intStr(1000000));
		Assert.assertEquals("1.000.000", Num.intStr(1000000d));
	}

	@Test
	public void testIntRound() {
		Assert.assertEquals("10", Num.intStr(10.4));
		Assert.assertEquals("1.000", Num.intStr(1000.5));
		Assert.assertEquals("1.000.001", Num.intStr(1000000.6));
	}

	@Test
	public void testDoubleFormat() {
		Assert.assertEquals("10,4", Num.str(10.4));
		Assert.assertEquals("1.000,45", Num.str(1000.445));
		Assert.assertEquals("1.000.000,45", Num.str(1000000.44556));
	}

	@Test
	public void readDouble() {
		Assert.assertEquals(0.99999, Num.read("0,99999"), 1e-10);
		Assert.assertEquals(1000000.45, Num.read("1.000.000,45"), 1e-10);
		Assert.assertEquals(1000000.44556, Num.read("1.000.000,44556"), 1e-10);
	}

}
