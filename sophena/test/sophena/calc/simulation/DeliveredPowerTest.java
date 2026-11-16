package sophena.calc.simulation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import sophena.model.Boiler;
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

/// A low-temperature producer can only cover a part of the load (the load
/// factor) but loads its surplus into the buffer tank. When the other
/// producers cannot cover the rest of the load, the uncovered load must be
/// calculated from the power that is delivered to the heat net and not from
/// the supplied power, which also contains the power that goes into the
/// buffer tank.
public class DeliveredPowerTest {

	@Test
	public void testUncoveredLoadWithLowTempProducer() {
		var r = EnergyResult.calculate(project());

		boolean hasLoadingWithUncoveredLoad = false;
		double expectedUncovered = 0;
		for (int h = 0; h < Stats.HOURS; h++) {
			double load = r.loadCurve[h];
			double delivered = r.deliveredPower[h];
			assertTrue(delivered <= load + 1e-9);
			assertTrue(r.suppliedPower[h] >= delivered - 1e-9);
			expectedUncovered += load - delivered;
			if (r.suppliedPower[h] > load && delivered < load - 1) {
				hasLoadingWithUncoveredLoad = true;
				assertEquals(load - delivered, r.uncoveredLoadAt(h), 1e-9);
			}
		}

		// the situation that we want to test must occur
		assertTrue(hasLoadingWithUncoveredLoad);
		assertEquals(expectedUncovered, r.totalUncoveredLoad(), 1e-6);

		// at most 50 + 20 kW can be delivered to the net, so at least 30 kW
		// of the 100 kW load are uncovered in every hour, except in the first
		// hours where the initial buffer content is used; when calculated from
		// the supplied power, the uncovered load was smaller (about the buffer
		// losses)
		assertTrue(r.totalUncoveredLoad() > 30.0 * (Stats.HOURS - 24));
		double fromSupplied = 0;
		for (int h = 0; h < Stats.HOURS; h++) {
			fromSupplied += Math.max(0, r.loadCurve[h] - r.suppliedPower[h]);
		}
		assertTrue(fromSupplied < 30.0 * (Stats.HOURS - 24));
	}

	/// A constant load of 100 kW, a heat net with 80 °C supply and 50 °C
	/// return temperature, a low-temperature producer with 65 °C (load factor
	/// 0.5) and up to 100 kW, and a high-temperature boiler with 20 kW.
	private Project project() {
		var project = new Project();
		project.costSettings = new CostSettings();
		project.weatherStation = new WeatherStation();
		project.weatherStation.data = new double[Stats.HOURS];
		HeatNet.addDefaultTo(project);
		var net = project.heatNet;
		// with dimensions, the buffer has losses that the low-temperature
		// producer reloads in every hour, while the load is not covered
		var buffer = new BufferTank();
		buffer.volume = 10_000;
		buffer.diameter = 2000;
		buffer.height = 3600;
		buffer.insulationThickness = 100;
		net.bufferTank = buffer;
		net.maximumPerformance = 500;

		var consumer = new Consumer();
		consumer.id = "consumer";
		consumer.profile = new LoadProfile();
		consumer.profile.staticData = new double[Stats.HOURS];
		consumer.profile.dynamicData = new double[Stats.HOURS];
		Arrays.fill(consumer.profile.dynamicData, 100);
		project.consumers.add(consumer);

		var lowTemp = new Producer();
		lowTemp.id = "low-temp";
		lowTemp.name = "low-temp";
		lowTemp.rank = 1;
		lowTemp.function = ProducerFunction.BASE_LOAD;
		lowTemp.productGroup = new ProductGroup();
		lowTemp.productGroup.type = ProductType.OTHER_HEAT_SOURCE;
		lowTemp.profile = ProducerProfile.initEmpty();
		Arrays.fill(lowTemp.profile.maxPower, 100);
		Arrays.fill(lowTemp.profile.temperaturLevel, 65);
		project.producers.add(lowTemp);

		var boiler = new Producer();
		boiler.id = "boiler";
		boiler.name = "boiler";
		boiler.rank = 2;
		boiler.function = ProducerFunction.PEAK_LOAD;
		boiler.productGroup = new ProductGroup();
		boiler.productGroup.type = ProductType.FOSSIL_FUEL_BOILER;
		boiler.boiler = new Boiler();
		boiler.boiler.maxPower = 20;
		project.producers.add(boiler);

		return project;
	}
}
