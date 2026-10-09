package sophena.calc.costs;

public class Annuity {

	private Annuity() {
	}

	/// Calculates the annuity of yearly costs according to VDI 2067. It first
	/// calculates the present value of these yearly costs and then the annuity
	/// of this present value, all related to the given number of years, the
	/// interest rate and price change factor.
	///
	/// @param years          The number of years.
	/// @param firstYearCosts The value of the first year.
	/// @param interestRate   The interest rate as percentage value, e.g. 2 means 2%.
	/// @param priceFactor    The yearly price change factor, e.g. 1.02.
	public static double ofYearlyCosts(
		int years, double firstYearCosts, double interestRate, double priceFactor
	) {
		double a = factor(years, interestRate);
		double b = presentValueFactor(years, interestRate, priceFactor);
		return firstYearCosts * b * a;
	}

	/// Calculates the annuity factor for the given time and interest rate.
	///
	/// @param years        The number of years.
	/// @param interestRate The interest rate as percentage value, e.g. 2 means 2%.
	public static double factor(int years, double interestRate) {
		if (years < 1)
			return 0.0;
		double T = years;
		double q = 1 + interestRate / 100.0;
		// when the interest rate is 0 (q = 1) the annuity factor is 1 / T
		if (Math.abs(q - 1) < 1e-10)
			return 1.0 / T;
		// the formula is often written a bit differently
		// (q - 1) / (1 - q^(-T))
		// = (q - 1) / (q^(-T) * (q^T - 1))
		// = q^T * (q - 1) / (q^T - 1)
		// = i * q^T / (q^T - 1)
		return (q - 1) / (1 - Math.pow(q, -T));
	}

	/// Calculates the present value factor for the given time, interest rate, and
	/// price change factor.
	///
	/// @param years        The number of years.
	/// @param interestRate The interest rate as percentage value, e.g. 2 means 2%.
	/// @param priceFactor  The yearly price change factor, e.g. 1.02.
	/// @de Barwertfaktor
	public static double presentValueFactor(
		int years,
		double interestRate,
		double priceFactor
	) {
		if (years < 1)
			return 0.0;
		double T = years;
		double q = 1 + interestRate / 100.0;
		double p = priceFactor;

		if (q <= 0)
			return 0;

		// Special case: q == p (interest rate matches price change)
		if (Math.abs(q - p) < 1e-6)
			return T / q;

		return (1 - Math.pow(p / q, T)) / (q - p);
	}

}
