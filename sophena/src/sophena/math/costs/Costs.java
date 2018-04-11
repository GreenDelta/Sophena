package sophena.math.costs;

import sophena.model.CostSettings;

public class Costs {

	private Costs() {
	}

	public static double gross(double netCosts, CostSettings settings) {
		if (netCosts == 0 || settings == null)
			return 0;
		double vat = 1 + settings.vatRate / 100;
		return netCosts * vat;
	}

}
