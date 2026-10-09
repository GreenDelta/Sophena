package sophena.calc.costs;

import org.junit.Assert;
import org.junit.Test;

public class AnnuityTest {

	@Test
	public void testGetCashValueFactor() {
		double priceChangeFactor = 1.02;
		double b = Annuity.presentValueFactor(20, 2, priceChangeFactor);
		Assert.assertEquals(19.6078431372549, b, 1e-10);
	}

}
