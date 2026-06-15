package sophena.calc.energy;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sophena.math.energetic.Producers;
import sophena.model.Boiler;
import sophena.model.HeatNet;
import sophena.model.HeatRecovery;
import sophena.model.Producer;
import sophena.model.Project;

public class StatePowerTest {

	private SimulationState state;
	private Producer p;

	@Before
	public void setUp() {
		p = new Producer();
		p.boiler = new Boiler();
		p.boiler.minPower = 130;
		p.boiler.maxPower = 200;
		p.boiler.efficiencyRate = 0.35;
		var project = new Project();
		project.heatNet = new HeatNet();
		project.producers.add(p);
		state = new SimulationState(project);
	}

	@Test
	public void testWithoutHeatRecovery() {
		int hour = 42;
		Assert.assertEquals(130, state.minPower(p, hour), 1e-10);
		Assert.assertEquals(200, state.maxPower(p, hour), 1e-10);
		Assert.assertEquals(200, Producers.maxPower(p), 1e-10);
		Assert.assertEquals(0.35, Producers.efficiencyRate(p), 1e-10);
	}

	@Test
	public void testWithHeatRecovery() {
		int hour = 42;
		p.heatRecovery = new HeatRecovery();
		p.heatRecovery.power = 100;
		p.heatRecovery.producerPower = 250;
		Assert.assertEquals(182, state.minPower(p, hour), 1e-10);
		Assert.assertEquals(280, state.maxPower(p, hour), 1e-10);
		Assert.assertEquals(280, Producers.maxPower(p), 1e-10);
		Assert.assertEquals(0.49, Producers.efficiencyRate(p), 1e-10);
	}
}
