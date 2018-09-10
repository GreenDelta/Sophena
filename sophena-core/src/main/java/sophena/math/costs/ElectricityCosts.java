package sophena.math.costs;

import sophena.math.energetic.UsedElectricity;
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
		// TODO: log calculation
		double amount = UsedElectricity.get(producedHeat, settings);
		double net = amount * settings.electricityPrice;
		return net;
	}

}
