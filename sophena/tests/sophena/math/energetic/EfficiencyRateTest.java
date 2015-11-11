package sophena.math.energetic;

import org.junit.Assert;
import org.junit.Test;

import sophena.math.energetic.EfficiencyRate;

public class EfficiencyRateTest {

	@Test
	public void testForBigBoiler() {
		double er = EfficiencyRate
				.ofBigBoiler()
				.utilisationRate(0.8835743527817865)
				.usageDuration_h(8760)
				.fullLoadHours_h(2000)
				.get();
		Assert.assertEquals(0.9, er, 1e-10);
	}

	@Test
	public void testFactor100() {
		double er = EfficiencyRate
				.ofBigBoiler()
				.utilisationRate(88.35743527817865)
				.usageDuration_h(8760)
				.fullLoadHours_h(2000)
				.get();
		Assert.assertEquals(90, er, 1e-10);
	}

	@Test
	public void testForSmallBoiler() {
		double er = EfficiencyRate
				.ofSmallBoiler()
				.utilisationRate(0.8593362105182752)
				.usageDuration_h(8760)
				.fullLoadHours_h(2000)
				.get();
		Assert.assertEquals(0.9, er, 1e-10);
	}

}
