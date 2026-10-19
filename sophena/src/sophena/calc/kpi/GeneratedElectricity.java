package sophena.calc.kpi;

import sophena.calc.ProjectResult;
import sophena.calc.specs.Producers;
import sophena.model.Producer;

public class GeneratedElectricity {

	/// Returns the generated electricity for the producer in the given project
	/// result in kWh.
	public static double get(Producer p, ProjectResult r) {
		// the generated electricity of a biogas plant is *not* allocated to
		// the project result.
		if (p == null
			|| r == null
			|| r.energyResult == null
			|| p.biogasPlant != null)
			return 0;
		double maxPower = Producers.electricPower(p);
		if (maxPower <= 0)
			return 0;
		double hours = Producers.fullLoadHours(p, r);
		return hours * maxPower;
	}

	/// Returns the total generated electricity of the given result in kWh.
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
