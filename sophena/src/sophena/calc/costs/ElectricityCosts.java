package sophena.calc.costs;

import sophena.calc.kpi.UsedElectricity;
import sophena.model.CostSettings;

/**
 * Contains functions to calculate the costs for electricity that are required
 * to produce a certain amount of heat.
 */
public class ElectricityCosts {

	private ElectricityCosts() {
	}

	public static double net(double producedHeat, CostSettings settings) {
		if (producedHeat == 0 || settings == null)
			return 0;
		double amount = UsedElectricity.get(producedHeat, settings);
		return amount * settings.electricityPrice;
	}

}
