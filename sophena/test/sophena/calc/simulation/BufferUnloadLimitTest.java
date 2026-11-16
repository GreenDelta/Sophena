package sophena.calc.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import sophena.model.BufferTank;
import sophena.model.Consumer;
import sophena.model.CostSettings;
import sophena.model.HeatNet;
import sophena.model.LoadProfile;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.ProducerProfile;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.model.Project;
import sophena.model.Stats;
import sophena.model.WeatherStation;

/// The maximum unload power of the buffer tank is a limit for the heat that
/// can be unloaded in an hour from all zones (HT, VT, NT) together, and not
/// for each zone separately.
public class BufferUnloadLimitTest {

	private static final double MAX_UNLOAD = 30;

	@Test
	public void testLimitAppliesToTheSumOfAllZones() {
		var r = EnergyResult.calculate(project());

		// in the first 4 hours, the producers cover the load and fill the
		// flow-temperature (VT) and high-temperature (HT) zone of the buffer
		for (int h = 0; h < 4; h++) {
			assertEquals(0, r.suppliedBufferHeat[h], 1e-9);
		}

		// then, both zones are filled but the buffer can only deliver the
		// maximum unload power per hour; before the fix, it delivered 60 kW
		// (30 kW from HT + 30 kW from VT)
		assertEquals(MAX_UNLOAD, r.suppliedBufferHeat[4], 1e-9);
		for (int h = 0; h < Stats.HOURS; h++) {
			assertTrue(r.suppliedBufferHeat[h] <= MAX_UNLOAD + 1e-9);
			assertTrue(r.deliveredPower[h] <= r.loadCurve[h] + 1e-9);
		}
	}

	/// A constant load of 100 kW and a buffer tank with a maximum unload power
	/// of 30 kW. A producer with 85 °C runs in the first 2 hours and fills the
	/// VT zone, a producer with 95 °C runs in hours 3 and 4 and fills the HT
	/// zone. After that, only the buffer can deliver heat.
	private Project project() {
		var project = new Project();
		project.costSettings = new CostSettings();
		project.weatherStation = new WeatherStation();
		project.weatherStation.data = new double[Stats.HOURS];
		HeatNet.addDefaultTo(project);
		var buffer = new BufferTank();
		buffer.volume = 10_000;
		project.heatNet.bufferTank = buffer;
		project.heatNet.maximumPerformance = MAX_UNLOAD;

		var consumer = new Consumer();
		consumer.id = "consumer";
		consumer.profile = new LoadProfile();
		consumer.profile.staticData = new double[Stats.HOURS];
		consumer.profile.dynamicData = new double[Stats.HOURS];
		Arrays.fill(consumer.profile.dynamicData, 100);
		project.consumers.add(consumer);

		project.producers.add(producer("VT", 1, 85, 0, 2));
		project.producers.add(producer("HT", 2, 95, 2, 4));
		return project;
	}

	private Producer producer(
		String id, int rank, double temperature, int from, int to
	) {
		var p = new Producer();
		p.id = id;
		p.name = id;
		p.rank = rank;
		p.function = ProducerFunction.MAX_LOAD;
		p.productGroup = new ProductGroup();
		p.productGroup.type = ProductType.OTHER_HEAT_SOURCE;
		p.profile = ProducerProfile.initEmpty();
		Arrays.fill(p.profile.temperaturLevel, temperature);
		for (int h = from; h < to; h++) {
			p.profile.maxPower[h] = 150;
		}
		return p;
	}
}
