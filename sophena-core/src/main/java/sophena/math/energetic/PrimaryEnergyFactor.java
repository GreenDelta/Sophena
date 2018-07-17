package sophena.math.energetic;

import sophena.Defaults;
import sophena.calc.EnergyResult;
import sophena.calc.ProjectResult;
import sophena.model.Fuel;
import sophena.model.Producer;

public class PrimaryEnergyFactor {

	private PrimaryEnergyFactor() {
	}

	public static double get(ProjectResult result) {
		if (result == null || result.project == null
				|| result.energyResult == null)
			return 0;
		double usedHeat = UsedHeat.get(result);
		if (usedHeat == 0)
			return 0;
		double sum = 0;
		EnergyResult r = result.energyResult;
		for (Producer p : r.producers) {
			double producedHeat = r.totalHeat(p);
			double fuelEnergy = result.fuelUsage.getInKWh(p);
			double fuelFactor = getFuelFactor(p);
			double usedElectricity = UsedElectricity.get(producedHeat,
					result.project.costSettings);
			double generatedElectricity = GeneratedElectricity.get(p,
					producedHeat);
			sum += ((fuelEnergy * fuelFactor)
					+ (usedElectricity - generatedElectricity)
							* Defaults.PRIMARY_ENERGY_FACTOR_ELECTRICITY);
		}
		double pf = sum / usedHeat;
		return pf < 0 ? 0 : pf;
	}

	private static double getFuelFactor(Producer p) {
		if (p == null || p.fuelSpec == null)
			return 0d;
		Fuel fuel = p.fuelSpec.fuel;
		return fuel != null ? fuel.primaryEnergyFactor : 0d;
	}
}
