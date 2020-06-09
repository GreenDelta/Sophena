package sophena.math.energetic;

import sophena.calc.ProjectResult;
import sophena.model.HeatRecovery;
import sophena.model.Producer;
import sophena.model.ProductType;
import sophena.model.Stats;

public class Producers {

	private Producers() {
	}

	/**
	 * Get the full load hours [h] for the given producer in the given result.
	 */
	public static double fullLoadHours(Producer p, ProjectResult r) {
		if (p == null || r == null || r.energyResult == null)
			return 0;
		double genHeat = r.energyResult.totalHeat(p);
		return fullLoadHours(p, genHeat);
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
		if (p == null)
			return 0;
		if (p.boiler != null)
			return p.boiler.efficiencyRate * heatRecoveryFactor(p);
		// for producer profiles we assume that the utilization rate
		// is the same as thermal efficiency rate
		return p.utilisationRate != null
				? p.utilisationRate
				: 0.0;
	}

	private static double heatRecoveryFactor(Producer p) {
		if (p == null || p.heatRecovery == null)
			return 1;
		HeatRecovery hr = p.heatRecovery;
		return 1 + (hr.power / hr.producerPower);
	}

	/**
	 * Returns the electric power of the given producer if it is a co-generation
	 * plant. Otherwise, returns 0.
	 */
	public static double electricPower(Producer p) {
		if (p == null || p.productGroup == null)
			return 0;
		if (p.productGroup.type != ProductType.COGENERATION_PLANT)
			return 0;
		if (p.boiler != null && p.boiler.isCoGenPlant)
			return p.boiler.maxPowerElectric;
		if (p.hasProfile())
			return p.profileMaxPowerElectric;
		return 0;
	}

	public static boolean isCoGenPlant(Producer p) {
		if (p == null)
			return false;
		if (p.boiler != null && p.boiler.isCoGenPlant)
			return true;
		if (p.productGroup == null ||
				p.productGroup.type != ProductType.COGENERATION_PLANT)
			return false;
		return p.hasProfile();
	}

	public static double electricalEfficiency(Producer p) {
		if (p == null || !isCoGenPlant(p))
			return 0.0;
		if (p.boiler != null)
			return p.boiler.efficiencyRateElectric;
		if (p.hasProfile()) {
			// for load profiles we assume that the
			// thermal efficiency rate is equals to
			// the utilization rate; see #19
			Double ur = p.utilisationRate;
			if (ur == null)
				return 0.0;
			if (p.profileMaxPower == 0.0
					|| p.profileMaxPowerElectric == 0.0)
				return 0.0;
			return ur * p.profileMaxPowerElectric / p.profileMaxPower;
		}
		return 0.0;
	}
}
