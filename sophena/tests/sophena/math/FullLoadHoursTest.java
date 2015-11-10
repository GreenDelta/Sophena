package sophena.math;

import org.junit.Assert;
import org.junit.Test;

public class FullLoadHoursTest {

	@Test
	public void test() {
		double fullLoadHours = FullLoadHours
				.boilerPower_kW(50)
				.generatedHeat_kWh(500)
				.get_h();
		Assert.assertEquals(10, fullLoadHours, 1e-10);
	}

}
