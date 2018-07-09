package sophena.math.costs;

import sophena.model.Project;

public class Costs {

	private Costs() {
	}

	/**
	 * Calculates the gross costs for the given net costs.
	 * 
	 * @param project
	 *            The project with the calculation settings.
	 * @param netCosts
	 *            The net costs that should be converted into gross costs.
	 */
	public static double gross(Project project, double netCosts) {
		if (netCosts == 0 || project == null || project.costSettings == null)
			return 0;
		double vat = 1 + project.costSettings.vatRate / 100;
		return netCosts * vat;
	}

	/**
	 * Calculate the annuity factor for the given project and interest rate.
	 * 
	 * @param project
	 *            The project with the calculation settings.
	 * @param interestRate
	 *            The percentage value of the interest rate (e.g. 2 means 2%).
	 */
	public static double annuityFactor(Project project, double interestRate) {
		if (project == null)
			return 0;
		double T = project.duration;
		double q = 1 + interestRate / 100;
		return (q - 1) / (1 - Math.pow(q, -T));
	}

	/**
	 * Calculate the cash value factor for the given project, interest rate, and
	 * price change factor.
	 * 
	 * @param project
	 *            The project with the calculation settings.
	 * @param interestRate
	 *            The percentage value of the interest rate (e.g. 2 means 2%).
	 * @param priceChangeFactor
	 *            The price change factor (e.g. 1.02)
	 */
	public static double cashValueFactor(Project project,
			double interestRate,
			double priceChangeFactor) {
		if (project == null)
			return 0;

		double q = 1 + interestRate / 100;
		double r = priceChangeFactor;
		double T = project.duration;

		if (Math.abs(q - r) < 1e-6)
			return T / q;

		return (1 - Math.pow(r / q, T)) / (q - r);
	}

}
