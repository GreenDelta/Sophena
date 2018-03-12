package sophena.math.energetic;

import org.junit.Assert;
import org.junit.Test;

import sophena.model.Boiler;
import sophena.model.Fuel;
import sophena.model.FuelConsumption;
import sophena.model.FuelGroup;
import sophena.model.FuelSpec;
import sophena.model.Producer;
import sophena.model.WoodAmountType;

public class CalorificValueTest {

	@Test
	public void testGetProducer() {
		double cv = CalorificValue.get(gasProducer());
		Assert.assertEquals(10, cv, 1e-10);
		double cvWood = CalorificValue.get(woodProducer());
		Assert.assertEquals(1334.459, cvWood, 1e-10);
	}

	@Test
	public void testGetFuelConsumption() {
		double cv = CalorificValue.get(gasConsumption());
		Assert.assertEquals(10, cv, 1e-10);
		double cvWood = CalorificValue.get(woodConsumption());
		Assert.assertEquals(1334.459, cvWood, 1e-10);
	}

	private Producer gasProducer() {
		Boiler boiler = new Boiler();
		boiler.fuel = gas();
		Producer producer = new Producer();
		producer.boiler = boiler;
		return producer;
	}

	private Producer woodProducer() {
		Boiler boiler = new Boiler();
		boiler.woodAmountType = WoodAmountType.LOGS;
		FuelSpec spec = new FuelSpec();
		spec.waterContent = 20;
		spec.woodFuel = wood();
		Producer p = new Producer();
		p.boiler = boiler;
		p.fuelSpec = spec;
		return p;
	}

	private FuelConsumption gasConsumption() {
		FuelConsumption c = new FuelConsumption();
		c.fuel = gas();
		return c;
	}

	private FuelConsumption woodConsumption() {
		FuelConsumption c = new FuelConsumption();
		c.fuel = wood();
		c.woodAmountType = WoodAmountType.LOGS;
		c.waterContent = 20;
		return c;
	}

	private Fuel gas() {
		Fuel fuel = new Fuel();
		fuel.name = "Gas";
		fuel.unit = "m3";
		fuel.calorificValue = 10; // kWh / m3
		return fuel;
	}

	private Fuel wood() {
		Fuel fuel = new Fuel();
		fuel.name = "Wood";
		fuel.group = FuelGroup.WOOD;
		fuel.calorificValue = 5.2; // kWh / kg dry mass
		fuel.density = 379; // kg / solid m3
		return fuel;
	}

}
