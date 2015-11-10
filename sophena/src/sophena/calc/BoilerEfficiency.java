package sophena.calc;

import sophena.model.Stats;

/**
 * Calculation functions for efficiency and utilization rates for small and big
 * boilers.
 */
public final class BoilerEfficiency {

	private BoilerEfficiency() {
	}

	/**
	 * Returns the estimated efficiency rate for small boilers with the given
	 * utilization rate and full load hours.
	 */
	public static double getEfficiencyRateSmall(double utilisationRate,
			int fullLoadHours) {
		if (fullLoadHours < 1)
			return 0;
		double se = getStandbyEfficiency(fullLoadHours, 0.014);
		return utilisationRate / se;
	}

	/**
	 * Returns the estimated efficiency rate for big boilers with the given
	 * utilization rate and full load hours.
	 */
	public static double getEfficiencyRateBig(double utilisationRate,
			int fullLoadHours) {
		if (fullLoadHours < 1)
			return 0;
		double se = getStandbyEfficiency(fullLoadHours, 0.0055);
		return utilisationRate / se;
	}

	private static double getStandbyEfficiency(double loadHours,
			double standbyLoss) {
		return 1.0 / ((Stats.HOURS / loadHours - 1.0) * standbyLoss + 1);
	}
}
