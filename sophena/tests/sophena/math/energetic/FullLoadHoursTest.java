package sophena.math.energetic;

import org.junit.Assert;
import org.junit.Test;

public class FullLoadHoursTest {

	@Test
	public void test() {
		double generatedHeat = 500;
		double boilerPower = 50;
		double fullLoadHours = FullLoadHours.get(generatedHeat, boilerPower);
		Assert.assertEquals(10, fullLoadHours, 1e-10);
	}

}
