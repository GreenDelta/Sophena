package sophena.math.energetic;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sophena.model.Boiler;
import sophena.model.HeatRecovery;
import sophena.model.Producer;

public class ProducersTest {

	private Producer p;

	@Before
	public void setUp() {
		p = new Producer();
		p.boiler = new Boiler();
		p.boiler.minPower = 130;
		p.boiler.maxPower = 200;
		p.boiler.efficiencyRate = 0.35;
	}

	@Test
	public void testWithoutHeatRecovery() {
		Assert.assertEquals(130, Producers.minPower(p), 1e-10);
		Assert.assertEquals(200, Producers.maxPower(p), 1e-10);
		Assert.assertEquals(0.35, Producers.efficiencyRate(p), 1e-10);
	}

	@Test
	public void testWithHeatRecovery() {
		p.heatRecovery = new HeatRecovery();
		p.heatRecovery.power = 100;
		p.heatRecovery.producerPower = 250;
		Assert.assertEquals(182, Producers.minPower(p), 1e-10);
		Assert.assertEquals(280, Producers.maxPower(p), 1e-10);
		Assert.assertEquals(0.49, Producers.efficiencyRate(p), 1e-10);
	}
}
