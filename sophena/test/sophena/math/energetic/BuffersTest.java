package sophena.math.energetic;

import org.junit.Assert;
import org.junit.Test;

import sophena.model.BufferTank;
import sophena.model.HeatNet;
import sophena.calc.BufferCalcState;

public class BuffersTest {

	@Test
	public void testMaxCapacity() {
		HeatNet net = new HeatNet();
		net.bufferTank = new BufferTank();
		net.bufferTank.volume = 10_000;
		net.maxBufferLoadTemperature = 80;
		net.lowerBufferLoadTemperature = 60d;
		Assert.assertEquals(233.2, BufferCalcState.maxCapacity(net), 1e-10);
	}

	@Test
	public void testLoss() {
		BufferTank buffer = new BufferTank();
		buffer.diameter = 2000;
		buffer.height = 3600;
		buffer.insulationThickness = 100;
		HeatNet net = new HeatNet();
		net.bufferTank = buffer;
		net.maxBufferLoadTemperature = 95;
		net.supplyTemperature = 80;
		net.bufferLambda = 0.04;
		double lossFactor = BufferCalcState.lossFactor(net);
		Assert.assertEquals(0.6565300327471949,
				BufferCalcState.loss(net, lossFactor, 0.5), 1e-10);
	}

}
