package sophena.math.energetic;

import org.junit.Assert;
import org.junit.Test;

public class EfficiencyRateTest {

	@Test
	public void test() {
		double er = EfficiencyRate.get(0.8593362105182752, 2000);
		Assert.assertEquals(0.9, er, 1e-10);
	}

	@Test
	public void testFactor100() {
		double er = EfficiencyRate.get(85.93362105182752, 2000);
		Assert.assertEquals(90, er, 1e-10);
	}

}
