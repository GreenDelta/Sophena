package sophena.math.energetic;

import sophena.Defaults;
import sophena.calc.EnergyResult;
import sophena.calc.ProjectResult;
import sophena.model.Producer;
import sophena.model.Project;

public class PrimaryEnergyFactor {

	private PrimaryEnergyFactor() {
	}

	public static double get(Project project, ProjectResult result) {
		if (project == null || result == null || result.energyResult == null)
			return 0;
		double usedHeat = UsedHeat.get(result);
		if (usedHeat == 0)
			return 0;
		double sum = 0;
		EnergyResult r = result.energyResult;
		for (Producer p : r.producers) {
			double producedHeat = r.totalHeat(p);
			double fuelEnergy = FuelDemand.getKWh(p, producedHeat);
			double fuelFactor = getFuelFactor(p);
			double usedElectricity = UsedElectricity.get(producedHeat,
					project.costSettings);
			double generatedElectricity = GeneratedElectricity.get(p,
					producedHeat);
			sum += ((fuelEnergy * fuelFactor)
					+ (usedElectricity - generatedElectricity)
							* Defaults.PRIMARY_ENERGY_FACTOR_ELECTRICITY);
		}
		return sum / usedHeat;
	}

	private static double getFuelFactor(Producer p) {
		if (p == null)
			return 0;
		if (p.fuelSpec != null && p.fuelSpec.woodFuel != null)
			return p.fuelSpec.woodFuel.primaryEnergyFactor;
		if (p.boiler != null && p.boiler.fuel != null)
			return p.boiler.fuel.primaryEnergyFactor;
		else
			return 0;
	}
}
