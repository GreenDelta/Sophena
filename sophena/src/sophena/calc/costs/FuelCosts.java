package sophena.calc.costs;

import sophena.math.FullLoadHours;
import sophena.math.UtilisationRate;
import sophena.model.Boiler;
import sophena.model.CostSettings;
import sophena.model.Fuel;
import sophena.model.FuelSpec;
import sophena.model.Producer;
import sophena.model.Stats;

/**
 * Functions for calculating costs related to fuel consumption.
 */
public class FuelCosts {

	private FuelCosts() {
	}

	public static double gross(Producer p, double producedHeat) {
		double net = net(p, producedHeat);
		if (net == 0 || p.fuelSpec == null)
			return 0;
		double vat = 1 + p.fuelSpec.taxRate / 100;
		return net * vat;
	}

	public static double net(Producer p, double producedHeat) {
		Boiler boiler = p.boiler;
		FuelSpec fuelSpec = p.fuelSpec;
		if (producedHeat == 0 || boiler == null || fuelSpec == null)
			return 0;

		double fullLoadHours = FullLoadHours
				.boilerPower_kW(boiler.maxPower)
				.generatedHeat_kWh(producedHeat)
				.get_h();

		double ur = UtilisationRate
				.forBigBoiler()
				.efficiencyRate(boiler.efficiencyRate)
				.fullLoadHours_h(fullLoadHours)
				.usageDuration_h(Stats.HOURS)
				.get();

		double energyContent = producedHeat / ur;
		Fuel fuel = boiler.fuel;
		if (fuel != null) {
			// no wood fuel
			double amount = energyContent / fuel.calorificValue;
			return amount * fuelSpec.pricePerUnit;
		}
		// wood fuel
		fuel = fuelSpec.woodFuel;
		if (boiler.woodAmountType == null || fuel == null)
			return 0;
		double wc = fuelSpec.waterContent / 100;
		double woodMass = energyContent
				/ ((1 - wc) * fuel.calorificValue - wc * 0.68);
		double woodAmount = woodMass * (1 - wc)
				/ (boiler.woodAmountType.getFactor() * fuel.density);
		return woodAmount * fuelSpec.pricePerUnit;
	}

	public static double getPriceChangeFactor(Producer p, CostSettings settings) {
		if (p == null || settings == null)
			return 0;
		if (p.boiler.fuel != null)
			return settings.fossilFuelFactor;
		else
			return settings.bioFuelFactor; // wood fuel
	}
}
