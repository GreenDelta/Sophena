package sophena.math.costs;

import sophena.model.CostSettings;
import sophena.model.FuelGroup;
import sophena.model.Producer;
import sophena.model.Project;

public class Fundings {

	public static double get(Project project) {
		if (project == null || project.costSettings == null)
			return 0;
		CostSettings settings = project.costSettings;
		return settings.funding +
				settings.fundingTransferStations
				+ getForBiomassBoilers(project)
				+ getForHeatNet(project);
	}

	private static double getForBiomassBoilers(Project project) {
		double f = project.costSettings.fundingBiomassBoilers; // EUR/kW
		if (f <= 0)
			return 0;
		double sum = 0;
		for (Producer p : project.producers) {
			if (p.disabled || p.productGroup == null)
				continue;
			if (!isBiomass(p.productGroup.fuelGroup))
				continue;
			if (p.boiler != null) {
				sum += f * p.boiler.maxPower;
			} else if (p.hasProfile) {
				sum += f * p.profileMaxPower;
			}
		}
		return sum;
	}

	private static boolean isBiomass(FuelGroup g) {
		if (g == null)
			return false;
		switch (g) {
		case BIOGAS:
		case PELLETS:
		case PLANTS_OIL:
		case WOOD:
			return true;
		default:
			return false;
		}
	}

	private static double getForHeatNet(Project project) {
		double f = project.costSettings.fundingHeatNet; // EUR/m
		if (f <= 0)
			return 0;
		return project.heatNet.length * f;
	}

}
