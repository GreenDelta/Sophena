package sophena.calc.costs;

public class Costs {

	private Costs() {
	}

	/// Calculates the annuity of a yearly value according to VDI 2067.
	///
	/// @param duration          The number of years.
	/// @param firstYearValue    The value of the first year.
	/// @param interestRate      The interest rate as percentage value, e.g. 2 means 2%.
	/// @param priceChangeFactor The price change factor, e.g. 1.02.
	public static double annuity(
		int duration,
		double firstYearValue,
		double interestRate,
		double priceChangeFactor
	) {
		double a = annuityFactor(duration, interestRate);
		double b = cashValueFactor(duration, interestRate, priceChangeFactor);
		return firstYearValue * a * b;
	}

	/// Calculates the annuity factor for the given time and interest rate.
	///
	/// @param duration     The number of years.
	/// @param interestRate The interest rate as percentage value, e.g. 2 means 2%.
	public static double annuityFactor(int duration, double interestRate) {
		if (duration < 1)
			return 0.0;
		double T = duration;
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

	/// Calculates the cash value factor for the given time, interest rate, and
	/// price change factor.
	///
	/// @param duration          The number of years.
	/// @param interestRate      The interest rate as percentage value, e.g. 2 means 2%.
	/// @param priceChangeFactor The price change factor, e.g. 1.02.
	public static double cashValueFactor(
		int duration,
		double interestRate,
		double priceChangeFactor
	) {
		if (duration < 1)
			return 0.0;
		double T = duration;
		double q = 1 + interestRate / 100.0;
		double p = priceChangeFactor;

		if (q <= 0)
			return 0;

		// Special case: q == p (interest rate matches price change)
		if (Math.abs(q - p) < 1e-6)
			return T / q;

		return (1 - Math.pow(p / q, T)) / (q - p);
	}

}
