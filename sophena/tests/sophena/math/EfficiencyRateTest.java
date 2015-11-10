package sophena.math;

import org.junit.Assert;
import org.junit.Test;

public class EfficiencyRateTest {

	@Test
	public void testForBigBoiler() {
		double er = EfficiencyRate
				.forBigBoiler()
				.utilisationRate(0.8835743527817865)
				.usageDuration_h(8760)
				.fullLoadHours_h(2000)
				.get();
		Assert.assertEquals(0.9, er, 1e-10);
	}

	@Test
	public void testFactor100() {
		double er = EfficiencyRate
				.forBigBoiler()
				.utilisationRate(88.35743527817865)
				.usageDuration_h(8760)
				.fullLoadHours_h(2000)
				.get();
		Assert.assertEquals(90, er, 1e-10);
	}

	@Test
	public void testForSmallBoiler() {
		double er = EfficiencyRate
				.forSmallBoiler()
				.utilisationRate(0.8593362105182752)
				.usageDuration_h(8760)
				.fullLoadHours_h(2000)
				.get();
		Assert.assertEquals(0.9, er, 1e-10);
	}

}
