package sophena.math.energetic;

import org.junit.Assert;
import org.junit.Test;

import sophena.model.Boiler;
import sophena.model.Producer;

public class GeneratedElectricityTest {

	@Test
	public void testProducer() {
		Producer p = new Producer();
		p.boiler = new Boiler();
		p.boiler.isCoGenPlant = true;
		p.boiler.maxPower = 250;
		p.boiler.maxPowerElectric = 200;
		double e = GeneratedElectricity.get(p, 250_000);
		Assert.assertEquals(200_000, e, 1e-16);
	}
}
