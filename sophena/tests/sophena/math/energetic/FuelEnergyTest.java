package sophena.math.energetic;

import org.junit.Assert;
import org.junit.Test;

public class FuelEnergyTest {

	@Test
	public void testGet() {
		double q = FuelEnergy
				.ofAmount_unit(10)
				.calorificValue_kWh_per_unit(10)
				.get_kWh();
		Assert.assertEquals(100, q, 1e-10);
	}

}
