package sophena.math.energetic;

import org.junit.Assert;
import org.junit.Test;

import sophena.model.CostSettings;

public class UsedElectricityTest {

	@Test
	public void testSimple() {
		CostSettings settings = new CostSettings();
		settings.electricityDemandShare = 1.5;
		double generatedHeat = 2000; // kWh
		double e = UsedElectricity.get(generatedHeat, settings);
		Assert.assertEquals(30, e, 1e-16);
	}

}
