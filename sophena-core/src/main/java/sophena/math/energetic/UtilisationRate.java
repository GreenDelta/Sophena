package sophena.math.energetic;

import sophena.Defaults;
import sophena.calc.EnergyResult;
import sophena.model.Producer;
import sophena.model.Stats;

/**
 * Calculates the utilisation rate of a boiler.
 */
public class UtilisationRate {

	public static double get(double efficiencyRate, int fullLoadHours) {
		return get(efficiencyRate, fullLoadHours, Stats.HOURS);
	}

	public static double get(double efficiencyRate, int fullLoadHours, int usageDuration) {
		if (fullLoadHours == 0)
			return 0;
		double ud = usageDuration;
		double fh = fullLoadHours;
		double standbyRate = 1 / ((ud / fh - 1) * Defaults.SPECIFIC_STAND_BY_LOSS + 1);
		return standbyRate * efficiencyRate;
	}

	public static double get(Producer producer, EnergyResult result) {
		if (producer == null || producer.boiler == null || result == null)
			return 0;
		if (producer.utilisationRate != null)
			return producer.utilisationRate;
		double generatedHeat = result.totalHeat(producer);
		int fullLoadHours = (int) Producers.fullLoadHours(producer, generatedHeat);
		int usageDuration = 8760; // UsageDuration.get(producer, result);
		double er = Producers.efficiencyRate(producer);
		return get(er, fullLoadHours, usageDuration);
	}
}
