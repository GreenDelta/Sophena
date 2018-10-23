package sophena.math.energetic;

import sophena.calc.ProjectResult;
import sophena.model.Producer;
import sophena.model.ProductType;

public class GeneratedElectricity {

	/**
	 * Returns the generated electricity [kWh] for the given producer and
	 * generated heat [kWh].
	 */
	public static double get(Producer p, ProjectResult r) {
		if (p == null || r == null || r.energyResult == null)
			return 0;
		if (p.productGroup == null
				|| p.productGroup.type != ProductType.COGENERATION_PLANT)
			return 0;
		double maxPower = 0;
		if (p.boiler != null && p.boiler.isCoGenPlant) {
			maxPower = p.boiler.maxPowerElectric;
		} else if (p.hasProfile()) {
			maxPower = p.profileMaxPowerElectric;
		}
		if (maxPower <= 0)
			return 0;
		double genHeat = r.energyResult.totalHeat(p);
		double hours = Producers.fullLoadHours(p, genHeat);
		return hours * maxPower;
	}

	/**
	 * Returns the total generated electricity of the given energy result.
	 */
	public static double getTotal(ProjectResult r) {
		if (r == null || r.energyResult == null)
			return 0;
		double total = 0;
		for (Producer p : r.energyResult.producers) {
			total += get(p, r);
		}
		return total;
	}

}
