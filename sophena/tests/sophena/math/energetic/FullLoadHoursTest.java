package sophena.math.energetic;

import org.junit.Assert;
import org.junit.Test;

import sophena.model.Boiler;
import sophena.model.Producer;

public class FullLoadHoursTest {

	@Test
	public void testWithBoiler() {
		Producer p = new Producer();
		p.boiler = new Boiler();
		p.boiler.maxPower = 50;
		double generatedHeat = 500;
		double fullLoadHours = Producers.fullLoadHours(p, generatedHeat);
		Assert.assertEquals(10, fullLoadHours, 1e-10);
	}

	@Test
	public void testWithProfiler() {
		Producer p = new Producer();
		p.hasProfile = true;
		p.profileMaxPower = 50;
		double generatedHeat = 500;
		double fullLoadHours = Producers.fullLoadHours(p, generatedHeat);
		Assert.assertEquals(10, fullLoadHours, 1e-10);
	}

}
