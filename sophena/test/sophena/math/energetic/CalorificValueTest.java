package sophena.math.energetic;

import org.junit.Assert;
import org.junit.Test;

import sophena.model.Fuel;
import sophena.model.FuelConsumption;
import sophena.model.FuelGroup;
import sophena.model.FuelSpec;
import sophena.model.WoodAmountType;

public class CalorificValueTest {

	@Test
	public void testFuelSpecNonWood() {
		FuelSpec spec = new FuelSpec();
		spec.fuel = gas();
		double cv = CalorificValue.get(spec);
		Assert.assertEquals(10, cv, 1e-10);
	}

	@Test
	public void testFuelSpecWetWoodChips() {
		FuelSpec spec = new FuelSpec();
		spec.fuel = wood();
		spec.waterContent = 20;
		spec.woodAmountType = WoodAmountType.CHIPS;
		double cv = CalorificValue.get(spec);
		Assert.assertEquals(762.548, cv, 1e-10);
	}

	@Test
	public void testFuelSpecDryWoodChips() {
		FuelSpec spec = new FuelSpec();
		spec.fuel = wood();
		spec.waterContent = 0;
		spec.woodAmountType = WoodAmountType.CHIPS;
		double cv = CalorificValue.get(spec);
		Assert.assertEquals(788.32, cv, 1e-10);
	}

	@Test
	public void testFuelSpecWetWoodTons() {
		FuelSpec spec = new FuelSpec();
		spec.fuel = wood();
		spec.waterContent = 20;
		spec.woodAmountType = WoodAmountType.MASS;
		double cv = CalorificValue.get(spec);
		Assert.assertEquals(4024.0, cv, 1e-10);
	}

	@Test
	public void testFuelSpecDryWoodTons() {
		FuelSpec spec = new FuelSpec();
		spec.fuel = wood();
		spec.waterContent = 0;
		spec.woodAmountType = WoodAmountType.MASS;
		double cv = CalorificValue.get(spec);
		Assert.assertEquals(5200.0, cv, 1e-10);
	}

	@Test
	public void testFuelSpecWetWoodLogs() {
		FuelSpec spec = new FuelSpec();
		spec.fuel = wood();
		spec.waterContent = 20;
		spec.woodAmountType = WoodAmountType.LOGS;
		double cv = CalorificValue.get(spec);
		Assert.assertEquals(1334.459, cv, 1e-10);
	}

	@Test
	public void testConsumptionNonWood() {
		FuelConsumption c = new FuelConsumption();
		c.fuel = gas();
		double cv = CalorificValue.get(c);
		Assert.assertEquals(10, cv, 1e-10);
	}

	@Test
	public void testConsumptionWood() {
		FuelConsumption c = new FuelConsumption();
		c.fuel = wood();
		c.woodAmountType = WoodAmountType.LOGS;
		c.waterContent = 20;
		double cv = CalorificValue.get(c);
		Assert.assertEquals(1334.459, cv, 1e-10);
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
		fuel.calorificValue = 5200; // kWh / t dry mass
		fuel.density = 379; // kg / solid m3
		return fuel;
	}
}
