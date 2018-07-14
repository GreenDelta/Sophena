package sophena.math.energetic;

import org.junit.Assert;
import org.junit.Test;

public class UtilisationRateTest {

	@Test
	public void test() {
		double ur = UtilisationRate.get(0.9, 2000);
		Assert.assertEquals(0.8593362105182752, ur, 1e-10);
	}

	@Test
	public void testFactor100() {
		double ur = UtilisationRate.get(90, 2000);
		Assert.assertEquals(85.93362105182752, ur, 1e-10);
	}

}
