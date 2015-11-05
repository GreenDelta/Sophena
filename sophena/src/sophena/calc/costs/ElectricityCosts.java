package sophena.calc.costs;

import sophena.model.CostSettings;

/**
 * Contains functions to calculate the costs for electricity that are required
 * to produce a certain amount of heat.
 */
public class ElectricityCosts {

	private ElectricityCosts() {
	}

	public static double gross(double producedHeat, CostSettings settings) {
		double net = net(producedHeat, settings);
		if (net == 0 || settings == null)
			return 0;
		double vat = 1 + settings.vatRate / 100;
		return net * vat;
	}

	public static double net(double producedHeat, CostSettings settings) {
		if (producedHeat == 0 || settings == null)
			return 0;
		double amount = producedHeat * settings.electricityDemandShare / 100;
		double net = amount * settings.electricityPrice;
		return net;
	}

}
