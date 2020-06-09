package sophena.model;

import org.junit.Assert;
import org.junit.Test;

public class StatsTest {

	@Test
	public void testNextStep() {
		Assert.assertEquals(1, Stats.nextStep(0.1));
		Assert.assertEquals(2, Stats.nextStep(1.1));
		Assert.assertEquals(22, Stats.nextStep(21.1));
		Assert.assertEquals(330, Stats.nextStep(321.1));
		Assert.assertEquals(4400, Stats.nextStep(4321.1));
		Assert.assertEquals(55000, Stats.nextStep(54321.1));
	}

}
