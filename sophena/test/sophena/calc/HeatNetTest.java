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
		net.powerLoss = 20; // [W/m]
		double[] curve = ProjectLoad.getNetLoadCurve(p);
		for (int i = 0; i < Stats.HOURS; i++) {
			Assert.assertEquals(10, curve[i], 1e-16);
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
		double powerLoss = HeatNets.calculatePowerLoss(net);
		assertEquals(7.3333333333, powerLoss, 1e-10);
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

		// 2000m of uno pipe with heatloss of 8W/m
		Pipe uno = new Pipe();
		uno.uValue = 8d / 80d;
		uno.pipeType = PipeType.UNO;
		HeatNetPipe hUno = new HeatNetPipe();
		net.pipes.add(hUno);
		hUno.pipe = uno;
		hUno.length = 2000;
		Assert.assertEquals(8d, HeatNets.getPowerLoss(hUno, net), 1e-16);

		// 1000m of duo pipe with heatloss of 12W/m
		Pipe duo = new Pipe();
		duo.uValue = 12d / 80d;
		duo.pipeType = PipeType.DUO;
		HeatNetPipe hDuo = new HeatNetPipe();
		net.pipes.add(hDuo);
		hDuo.pipe = duo;
		hDuo.length = 1000;
		Assert.assertEquals(12d, HeatNets.getPowerLoss(hDuo, net), 1e-16);

		// net length is 2000m
		Assert.assertEquals(2000d, HeatNets.getTrenchLengthOf(net), 1e-16);

		// net loss = (2000m * 8W/m + 1000m * 12W/m) / 2000m = 14W/m
		Assert.assertEquals(14d, HeatNets.calculatePowerLoss(net), 1e-16);
	}
}
