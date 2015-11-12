package sophena.math.costs;

import org.junit.Assert;
import org.junit.Test;

public class AnnuitiyFactorTest {

	@Test
	public void testGet() {
		double af = AnnuitiyFactor
				.ofInterestRate(0.02)
				.withDuration_years(20)
				.get();
		Assert.assertEquals(0.06115671812529034, af, 1e-10);
	}

}
