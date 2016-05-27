package sophena.math.energetic;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sophena.calc.EnergyResult;
import sophena.model.Producer;
import sophena.model.Stats;

public class UsageDurationTest {

	private Producer p;
	private EnergyResult result;
	private double[] values;

	@Before
	public void setUp() {
		p = new Producer();
		result = new EnergyResult();
		result.producers = new Producer[] { p };
		values = new double[Stats.HOURS];
		result.producerResults = new double[][] { values };
	}

	@Test
	public void testEmpty() {
		Assert.assertEquals(0, UsageDuration.get(result, p));
	}

	@Test
	public void testPartly() {
		for (int i = 1000; i < 4000; i++) {
			values[i] = 42;
		}
		Assert.assertEquals(3000, UsageDuration.get(result, p));
	}

	@Test
	public void testFull() {
		for (int i = 0; i < Stats.HOURS; i++) {
			values[i] = 42;
		}
		Assert.assertEquals(Stats.HOURS, UsageDuration.get(result, p));
	}
}
