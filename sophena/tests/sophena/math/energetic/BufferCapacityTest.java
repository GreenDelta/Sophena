package sophena.math.energetic;

import org.junit.Assert;
import org.junit.Test;

public class BufferCapacityTest {

	@Test
	public void testGet_kWh() {
		BufferCapacity cap = new BufferCapacity();
		cap.volume = 10_000;
		cap.maxChargingTemperatur = 80;
		cap.lowerChargingTemperatur = 60;
		Assert.assertEquals(233.2, cap.value(), 1e-10);
	}

}
