package sophena.math.energetic;

import org.junit.Assert;
import org.junit.Test;

import sophena.math.energetic.UtilisationRate;

public class UtilisationRateTest {

	@Test
	public void testForBigBoiler() {
		double ur = UtilisationRate
				.ofBigBoiler()
				.efficiencyRate(0.9)
				.usageDuration_h(8760)
				.fullLoadHours_h(2000)
				.get();
		Assert.assertEquals(0.8835743527817865, ur, 1e-10);
	}

	@Test
	public void testFactor100() {
		double ur = UtilisationRate
				.ofBigBoiler()
				.efficiencyRate(90)
				.usageDuration_h(8760)
				.fullLoadHours_h(2000)
				.get();
		Assert.assertEquals(88.35743527817865, ur, 1e-10);
	}

	@Test
	public void testForSmallBoiler() {
		double ur = UtilisationRate
				.ofSmallBoiler()
				.efficiencyRate(0.9)
				.usageDuration_h(8760)
				.fullLoadHours_h(2000)
				.get();
		Assert.assertEquals(0.8593362105182752, ur, 1e-10);
	}

}
