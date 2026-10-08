package sophena.calc.costs;

import sophena.calc.ProjectResult;
import sophena.model.Project;

public class Costs {

	private Costs() {
	}

	public static double annuity(ProjectResult r, double firstYearValue,
			double interestRate, double priceChangeFactor) {
		double a = annuityFactor(r.project, interestRate);
		double b = cashValueFactor(r.project, interestRate, priceChangeFactor);
		double annuity = firstYearValue * a * b;
		return annuity;
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
