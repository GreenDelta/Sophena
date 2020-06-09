package sophena.math.energetic;

import sophena.calc.ProjectResult;
import sophena.model.Producer;

public class GeneratedElectricity {

	/**
	 * Returns the generated electricity [kWh] for the given producer and
	 * generated heat [kWh].
	 */
	public static double get(Producer p, ProjectResult r) {
		if (p == null || r == null || r.energyResult == null)
			return 0;
		double maxPower = Producers.electricPower(p);
		if (maxPower <= 0)
			return 0;
		double hours = Producers.fullLoadHours(p, r);
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
