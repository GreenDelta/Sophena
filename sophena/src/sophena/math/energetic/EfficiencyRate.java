package sophena.math.energetic;

import sophena.Defaults;
import sophena.model.Stats;

public class EfficiencyRate {

	/**
	 * Calculates the efficiency rate from the given utilization rate and full
	 * load hours assuming a total usage time of 8760 hours.
	 */
	public static double get(double utilisationRate, int fullLoadHours) {
		return get(utilisationRate, fullLoadHours, Stats.HOURS);
	}

	/**
	 * Calculates the efficiency rate from the given utilization rate, full load
	 * hours [h], and total usage time [h].
	 */
	public static double get(double utilisationRate, int fullLoadHours, int totalHours) {
		if (fullLoadHours == 0)
			return 0;
		double fh = fullLoadHours;
		double th = totalHours;
		return utilisationRate * ((th / fh - 1) * Defaults.SPECIFIC_STAND_BY_LOSS + 1);
	}
}
