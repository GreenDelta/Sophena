package sophena.calc.kpi;

import sophena.Defaults;
import sophena.calc.ProjectResult;
import sophena.model.Fuel;
import sophena.model.Producer;

public class PrimaryEnergyFactor {

	private PrimaryEnergyFactor() {
	}

	public static double get(ProjectResult r) {
		if (r == null || r.project == null
			|| r.project.costSettings == null
			|| r.energyResult == null)
			return 0;
		double usedHeat = UsedHeat.get(r);
		if (usedHeat == 0)
			return 0;
		double sum = 0;
		for (Producer p : r.energyResult.producers) {

			// for consumed fuel
			double fuelEnergy = r.fuelUsage.getInKWh(p);
			sum += (fuelEnergy * factor(p));

			// for used electricity
			double genHeat = r.energyResult.totalHeat(p);
			double usedElectricity = UsedElectricity.get(
				genHeat, r.project.costSettings);
			sum += (usedElectricity * factor(
				r.project.costSettings.projectElectricityMix,
				Defaults.PRIMARY_ENERGY_FACTOR_ELECTRICITY));

			// for generated electricity
			double genElectricity = GeneratedElectricity.get(p, r);
			if (genElectricity > 0) {
				sum -= (genElectricity * factor(
					p.producedElectricity,
					Defaults.PRIMARY_ENERGY_FACTOR_ELECTRICITY));
			}
		}
		double pf = sum / usedHeat;
		return pf < 0 ? 0 : pf;
	}

	private static double factor(Producer p) {
		return p != null && p.fuelSpec != null
			? factor(p.fuelSpec.fuel, 0)
			: 0;
	}

	private static double factor(Fuel f, double defaultVal) {
		if (f == null)
			return defaultVal;
		return f.primaryEnergyFactor;
	}
}
