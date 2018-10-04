package sophena.math.energetic;

import org.junit.Assert;
import org.junit.Test;

import sophena.model.HeatNet;

public class BuffersTest {

	@Test
	public void testMaxCapacity() {
		HeatNet net = new HeatNet();
		net.bufferTankVolume = 10_000;
		net.maxBufferLoadTemperature = 80;
		net.lowerBufferLoadTemperature = 60d;
		Assert.assertEquals(233.2, Buffers.maxCapacity(net), 1e-10);
	}

}
