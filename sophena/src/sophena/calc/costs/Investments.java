package sophena.calc.costs;

/// Shared functions for the cost calculations of investment items that are used
/// by the cost calculators of projects and biogas plants.
public final class Investments {

	private Investments() {
	}

	/// Calculates the annuity of the capital costs of the given item according
	/// to VDI 2067. The amount that is spent initially can be smaller than the
	/// amount that is spent for a replacement, e.g. for the refurbishment of an
	/// existing asset.
	public static double capitalCosts(
		InvestmentItem item,
		int T,
		double interestRate,
		double priceChange
	) {
		if (item == null)
			return 0;
		return capitalCosts(
			item.initialInvestment(),
			item.investment(),
			item.duration(),
			T,
			interestRate,
			priceChange);
	}

	/// Calculates the annuity of the capital costs for an investment where the
	/// amount that is spent initially is given separately from the amount that
	/// is spent for a replacement. A missing duration falls back to the
	/// observation period.
	public static double capitalCosts(
		double initial,
		double replacement,
		int duration,
		int T,
		double interestRate,
		double priceChange
	) {
		if (replacement <= 0)
			return 0;
		int Tu = duration > 0 ? duration : T;
		if (Tu <= 0 || T <= 0)
			return 0;
		double q = 1 + interestRate / 100;
		return CapitalCosts.calculate(
			initial, replacement, Tu, T, q, priceChange);
	}

	/// The yearly maintenance and repair costs of the given item. These costs
	/// are always applied to the full investment, also for refurbishments.
	public static double maintenanceBase(InvestmentItem item) {
		if (item == null)
			return 0;
		return maintenanceBase(
			item.investment(),
			item.repair(),
			item.maintenance());
	}

	/// The yearly maintenance and repair costs for the given investment.
	public static double maintenanceBase(
		double investment, double repair, double maintenance
	) {
		return investment * (repair + maintenance) / 100;
	}
}
