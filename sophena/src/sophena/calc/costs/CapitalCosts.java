package sophena.calc.costs;

import static java.lang.Math.pow;

import sophena.model.Project;

public class CapitalCosts {

	private CapitalCosts() {
	}

	/**
	 * Get the capital costs for the given component.
	 *
	 * @param item
	 *            The component for which the capital costs should be
	 *            calculated.
	 * @param project
	 *            The project with the calculation settings.
	 * @param interestRate
	 *            The percentage value of the interest rate that should be used
	 *            in the calculation (e.g. 2)
	 * @param priceChangeFactor
	 *            The price change factor that should be used in the calculation
	 *            (e.g. 1.02).
	 */
	public static double get(CostResultItem item, Project project,
			double interestRate, double priceChangeFactor) {
		if (item == null || item.costs == null
				|| project == null || project.costSettings == null)
			return 0;
		double interestFactor = 1 + interestRate / 100;
		return calculate(
				item.investmentCosts,
				item.costs.duration,
				project.duration,
				interestFactor,
				priceChangeFactor);
	}

	/**
	 * Low level function to calculate the capital costs of a component
	 * according to VDI 2067.
	 *
	 * @param A
	 *            Investment amount (in EUR)
	 * @param Tu
	 *            service life (in years) of the component
	 * @param T
	 *            obervation period (in years)
	 * @param q
	 *            interest factor (e.g. 1.02)
	 * @param r
	 *            price change factor (e.g. 1.02)
	 */
	public static double calculate(
			double A,
			int Tu,
			int T,
			double q,
			double r) {
		return calculate(A, A, Tu, T, q, r);
	}

	/**
	 * Low level function to calculate the capital costs of a component
	 * according to VDI 2067. In contrast to
	 * {@link #calculate(double, int, int, double, double)} the amount that is
	 * spent initially can be different from the amount that is spent for a
	 * replacement. This is the case for the refurbishment or general overhaul of
	 * an existing asset: only a share of the investment is spent initially while
	 * a replacement of the asset costs the full investment.
	 *
	 * @param initial
	 *            The amount that is spent initially (in EUR)
	 * @param replacement
	 *            The amount that is spent for a replacement (in EUR)
	 * @param Tu
	 *            service life (in years) of the component
	 * @param T
	 *            obervation period (in years)
	 * @param q
	 *            interest factor (e.g. 1.02)
	 * @param r
	 *            price change factor (e.g. 1.02)
	 */
	public static double calculate(
			double initial,
			double replacement,
			int Tu,
			int T,
			double q,
			double r) {

		double sum = initial;

		// add cash values of possible replacements
		int t = Tu;
		int n = 0; // the number of replacements
		while (t > 0 && t < T) {
			sum += replacement * pow(r, t) / pow(q, t);
			n++;
			t += Tu;
		}

		// remove a possible residual value; the last investment is the initial
		// one when there was no replacement
		int Tr = Tu * (1 + n) - T;
		if (Tr > 0) {
			double last = n == 0 ? initial : replacement;
			double factor = ((double) ((n + 1) * Tu - T)) / ((double) Tu);
			double Rv = last * pow(r, n * Tu) * factor * 1 / pow(q, T);
			sum -= Rv;
		}

		// apply the annuity factor; if q = 1 -> 1/T
		double a = Math.abs(q - 1) < 1e-10
			? 1.0 / T
			: (q - 1) / (1 - pow(q, -T));
		return sum * a;
	}
}
