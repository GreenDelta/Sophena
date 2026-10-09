package sophena.calc.costs;

import org.junit.Assert;
import org.junit.Test;

public class AnnuityFactorTest {

	@Test
	public void testGetForDuration() {
		double af = Annuity.factor(20, 2);
		Assert.assertEquals(0.0611567181252903, af, 1e-10);
	}

}
