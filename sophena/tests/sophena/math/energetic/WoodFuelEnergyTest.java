package sophena.math.energetic;

import org.junit.Assert;
import org.junit.Test;

public class WoodFuelEnergyTest {

	@Test
	public void testGet() {
		double q = WoodFuelEnergy
				.ofWoodMass_kg(10)
				.waterContent(0.2)
				.calorificValue_kWh_per_kg(5)
				.get_kWh();
		Assert.assertEquals(38.64, q, 1e-10);
	}

}
