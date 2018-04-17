package sophena.math.costs;

import sophena.model.CostSettings;
import sophena.model.Project;

public class Costs {

	private Costs() {
	}

	public static double gross(double netCosts, CostSettings settings) {
		if (netCosts == 0 || settings == null)
			return 0;
		double vat = 1 + settings.vatRate / 100;
		return netCosts * vat;
	}

	public static double annuityFactor(Project project, double interestRate) {
		if (project == null)
			return 0;
		double n = project.duration;
		double q = 1 + interestRate / 100;
		return (q - 1) / (1 - Math.pow(q, -n));
	}

}
