package sophena.math.energetic;

import org.junit.Assert;
import org.junit.Test;

public class BufferCapacityTest {

	@Test
	public void testGet_kWh() {
		double q = BufferCapacity
				.ofVolume_L(10000)
				.maxChargingTemperatur_degC(80)
				.returnTemperature_degC(60)
				.get_kWh();
		Assert.assertEquals(233.2, q, 1e-10);
	}

}
