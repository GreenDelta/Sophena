package sophena.calc;

import sophena.model.Stats;

/**
 * Calculation functions for efficiency and utilization rates for small and big
 * boilers.
 */
public final class BoilerEfficiency {

	private BoilerEfficiency() {
	}

	private static double getStandbyEfficiency(double loadHours,
			double standbyLoss) {
		return 1.0 / ((Stats.HOURS / loadHours - 1.0) * standbyLoss + 1);
	}

	public static double getUtilisationRateSmall(double efficiencyRate,
			int fullLoadHours) {
		if (fullLoadHours < 1)
			return 0;
		double se = getStandbyEfficiency(fullLoadHours, 0.014);
		return efficiencyRate * se;
	}

	public static double getEfficiencyRateSmall(double utilisationRate,
			int fullLoadHours) {
		if (fullLoadHours < 1)
			return 0;
		double se = getStandbyEfficiency(fullLoadHours, 0.014);
		return utilisationRate / se;
	}

	public static double getUtilisationRateBig(double efficiencyRate,
			int fullLoadHours) {
		if (fullLoadHours < 1)
			return 0;
		double se = getStandbyEfficiency(fullLoadHours, 0.0055);
		return efficiencyRate * se;
	}

	public static double getEfficiencyRateBig(double utilisationRate,
			int fullLoadHours) {
		if (fullLoadHours < 1)
			return 0;
		double se = getStandbyEfficiency(fullLoadHours, 0.0055);
		return utilisationRate / se;
	}
}
