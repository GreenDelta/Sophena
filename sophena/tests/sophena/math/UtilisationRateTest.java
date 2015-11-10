package sophena.math;

import org.junit.Assert;
import org.junit.Test;

public class UtilisationRateTest {

	@Test
	public void testForBigBoiler() {
		double ur = UtilisationRate
				.forBigBoiler()
				.efficiencyRate(0.9)
				.usageDuration_h(8760)
				.fullLoadHours_h(2000)
				.get();
		Assert.assertEquals(0.8835743527817865, ur, 1e-10);
	}

	@Test
	public void testForSmallBoiler() {
		double ur = UtilisationRate
				.forSmallBoiler()
				.efficiencyRate(0.9)
				.usageDuration_h(8760)
				.fullLoadHours_h(2000)
				.get();
		Assert.assertEquals(0.8593362105182752, ur, 1e-10);
	}

}
