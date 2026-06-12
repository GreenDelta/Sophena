package sophena.calc;

import static org.junit.Assert.assertEquals;

import org.junit.Assert;
import org.junit.Test;

import sophena.math.energetic.HeatNets;
import sophena.model.HeatNet;
import sophena.model.HeatNetPipe;
import sophena.model.Pipe;
import sophena.model.PipeType;
import sophena.model.Project;
import sophena.model.Stats;

public class HeatNetTest {

	@Test
	public void testSimpleLoad() {
		Project p = new Project();
		HeatNet net = p.heatNet = new HeatNet();
		net.length = 500; // [m]
		// total heat loss coefficient of the net in W/K
		net.powerLoss = 20;
		double[] curve = ProjectLoad.getNetLoadCurve(p);
		// no weather station → fallback assumes 50 K delta:
		// 50 K * 20 W/K / 1000 = 1.0 kW
		for (int i = 0; i < Stats.HOURS; i++) {
			Assert.assertEquals(1.0, curve[i], 1e-16);
		}
	}

	@Test
	public void testCalculateLength() {
		HeatNet net = buildHeatNet();
		double length = HeatNets.getTrenchLengthOf(net);
		Assert.assertEquals(60, length, 1e-16);
	}

	@Test
	public void testCalculatePowerLoss() {
		HeatNet net = buildHeatNet();
		// sum of pipe.length * uValue = 30*0.1 + 20*0.15 + 10*0.2 = 8.0 W/K total
		double powerLoss = HeatNets.heatLossCoefficientOf(net);
		assertEquals(8.0, powerLoss, 1e-10);
	}

	private HeatNet buildHeatNet() {
		HeatNet net = new HeatNet();
		net.supplyTemperature = 80;
		net.returnTemperature = 50;
		addPipe(net, 30, 0.1);
		addPipe(net, 20, 0.15);
		addPipe(net, 10, 0.2);
		return net;
	}

	private void addPipe(HeatNet net, double length, double uValue) {
		Pipe pipe = new Pipe();
		pipe.uValue = uValue;
		HeatNetPipe netPipe = new HeatNetPipe();
		netPipe.pipe = pipe;
		netPipe.length = length;
		net.pipes.add(netPipe);
	}

	@Test
	public void testUnoDouPipes() {
		HeatNet net = new HeatNet();
		net.supplyTemperature = 100;
		net.returnTemperature = 80;

		// 2000m uno pipe with uValue = 0.1 W/(m·K)
		// heat loss coefficient = length * uValue = 2000 * 0.1 = 200 W/K
		var uno = new Pipe();
		uno.uValue = 0.1;
		uno.pipeType = PipeType.UNO;
		HeatNetPipe hUno = new HeatNetPipe();
		net.pipes.add(hUno);
		hUno.pipe = uno;
		hUno.length = 2000;
		Assert.assertEquals(200.0, HeatNets.heatLossCoefficientOf(hUno), 1e-16);

		// 1000m duo pipe with uValue = 0.15 W/(m·K)
		// heat loss coefficient = 1000 * 0.15 = 150 W/K
		Pipe duo = new Pipe();
		duo.uValue = 0.15;
		duo.pipeType = PipeType.DUO;
		HeatNetPipe hDuo = new HeatNetPipe();
		net.pipes.add(hDuo);
		hDuo.pipe = duo;
		hDuo.length = 1000;
		Assert.assertEquals(150.0, HeatNets.heatLossCoefficientOf(hDuo), 1e-16);

		// trench length: 2000/2 (UNO) + 1000 (DUO) = 2000 m
		Assert.assertEquals(2000d, HeatNets.getTrenchLengthOf(net), 1e-16);

		// total coefficient = 200 + 150 = 350 W/K
		Assert.assertEquals(350.0, HeatNets.heatLossCoefficientOf(net), 1e-16);
	}
}
