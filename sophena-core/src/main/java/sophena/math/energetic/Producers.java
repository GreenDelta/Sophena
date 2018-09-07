package sophena.math.energetic;

import sophena.model.HeatRecovery;
import sophena.model.Producer;
import sophena.model.Stats;

public class Producers {

	private Producers() {
	}

	/**
	 * Returns the full load hours [h] for the given producer specification and
	 * generated heat [kWh].
	 */
	public static double fullLoadHours(Producer p, double generatedHeat) {
		if (p == null)
			return 0;
		double P_max = maxPower(p);
		if (P_max == 0)
			return 0;
		return Math.ceil(generatedHeat / P_max);
	}

	/**
	 * Get the minimum power of the given producer for the given hour (used in
	 * energy simulations).
	 */
	public static double minPower(Producer p, int hour) {
		if (p == null)
			return 0;
		if (p.hasProfile()) {
			if (p.profile == null)
				return 0;
			return Stats.get(p.profile.minPower, hour);
		}
		if (p.boiler == null)
			return 0;
		return p.boiler.minPower * heatRecoveryFactor(p);
	}

	/**
	 * Get the minimum power of the given producer for the given hour (used in
	 * energy simulations).
	 */
	public static double maxPower(Producer p, int hour) {
		if (p == null)
			return 0;
		if (p.hasProfile()) {
			if (p.profile == null)
				return 0;
			return Stats.get(p.profile.maxPower, hour);
		}
		if (p.boiler == null)
			return 0;
		return p.boiler.maxPower * heatRecoveryFactor(p);
	}

	public static double maxPower(Producer p) {
		if (p == null)
			return 0;
		if (p.hasProfile())
			return p.profileMaxPower;
		if (p.boiler == null)
			return 0;
		return p.boiler.maxPower * heatRecoveryFactor(p);
	}

	/**
	 * Calculates the difference between the sum of the maximum powers of the
	 * given producers and the given load.
	 */
	public static double powerDifference(Producer[] producers, double load) {
		double power = 0;
		for (Producer p : producers) {
			power += maxPower(p);
		}
		return power - load;
	}

	public static double efficiencyRate(Producer p) {
		// TODO: producer profiles ...
		if (p == null || p.boiler == null)
			return 0;
		return p.boiler.efficiencyRate * heatRecoveryFactor(p);
	}

	private static double heatRecoveryFactor(Producer p) {
		if (p == null || p.heatRecovery == null)
			return 1;
		HeatRecovery hr = p.heatRecovery;
		return 1 + (hr.power / hr.producerPower);
	}
}
