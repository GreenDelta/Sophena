package sophena.calc;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import sophena.math.energetic.Producers;
import sophena.model.Consumer;
import sophena.model.Fuel;
import sophena.model.FuelGroup;
import sophena.model.FuelSpec;
import sophena.model.HeatNet;
import sophena.model.LoadProfile;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.ProducerProfile;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.model.Project;
import sophena.model.Stats;

public class ProfileTest {

	private ProjectResult result;
	private Producer producer;

	@Before
	public void setUp() {

		Project project = new Project();
		project.id = "p";
		project.heatNet = new HeatNet();

		Consumer consumer = new Consumer();
		project.consumers.add(consumer);
		consumer.id = "c";
		consumer.profile = new LoadProfile();
		consumer.profile.staticData = new double[Stats.HOURS];
		consumer.profile.dynamicData = new double[Stats.HOURS];
		for (int i = 0; i < Stats.HOURS; i++) {
			consumer.profile.staticData[i] = 2100;
			consumer.profile.dynamicData[i] = 2100;
		}

		producer = new Producer();
		project.producers.add(producer);
		producer.id = "pro";
		producer.profile = new ProducerProfile();
		producer.profile.minPower = new double[Stats.HOURS];
		producer.profile.maxPower = new double[Stats.HOURS];
		for (int i = 0; i < Stats.HOURS; i++) {
			producer.profile.minPower[i] = 3000;
			producer.profile.maxPower[i] = 5000;
		}
		producer.function = ProducerFunction.BASE_LOAD;
		producer.rank = 1;
		producer.utilisationRate = 0.8;
		producer.profileMaxPower = 5000;
		producer.profileMaxPowerElectric = 3000;
		producer.productGroup = new ProductGroup();
		producer.productGroup.type = ProductType.COGENERATION_PLANT;

		// fuel spec
		Fuel gas = new Fuel();
		gas.group = FuelGroup.NATURAL_GAS;
		gas.calorificValue = 10.0; // kWh/m3
		gas.unit = "m3";
		FuelSpec spec = new FuelSpec();
		spec.fuel = gas;
		spec.pricePerUnit = 0.4; // EUR/m3
		producer.fuelSpec = spec;

		result = ProjectResult.calculate(project);
	}

	@Test
	public void testGeneratedHeat() {
		assertEquals(Stats.HOURS * 4200,
				result.energyResult.totalProducedHeat, 1e-6);
		double fullLoadHours = Math.ceil(Stats.HOURS * 4200.0 / 5000.0);
		assertEquals(fullLoadHours,
				Producers.fullLoadHours(producer, result), 1e-6);
	}

}
