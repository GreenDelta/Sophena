package sophena.math.energetic;

import org.junit.Assert;
import org.junit.Test;

public class WoodAmountTest {

	@Test
	public void testGetStere() {
		double wa = WoodAmount
				.ofMass_kg(50)
				.waterContent(0.2)
				.woodDensity_kg_per_m3(379)
				.getAmountInLogs_stere();
		Assert.assertEquals(0.15077271013946475, wa, 1e-10);
	}

}
