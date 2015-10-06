package sophena.calc;

import org.junit.Assert;
import org.junit.Test;

public class BoilerEfficiencyTest {

	@Test
	public void testGetUtilRate() {
		double urSmall = BoilerEfficiency.getUtilisationRateSmall(90, 1800);
		Assert.assertEquals(85.37819377684, urSmall, 1e-10);
		double urBig = BoilerEfficiency.getUtilisationRateBig(90, 1800);
		Assert.assertEquals(88.125856779147, urBig, 1e-10);
	}

	@Test
	public void testGetEffiRate() {
		double erSmall = BoilerEfficiency.getEfficiencyRateSmall(90, 1800);
		Assert.assertEquals(94.872000000042, erSmall, 1e-10);
		double erBig = BoilerEfficiency.getEfficiencyRateBig(90, 1800);
		Assert.assertEquals(91.9140000000165, erBig, 1e-10);
	}

}
