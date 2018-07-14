package sophena.math.costs;

import sophena.model.Consumer;
import sophena.model.Producer;
import sophena.model.ProductCosts;
import sophena.model.ProductType;
import sophena.model.Project;

public class Fundings {

	public static double get(Project project) {
		if (project == null || project.costSettings == null)
			return 0;
		return project.costSettings.funding
				+ getForTransferStations(project)
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
			ProductType type = p.productGroup.type;
			if (p.boiler != null && type == ProductType.BIOMASS_BOILER) {
				sum += f * p.boiler.maxPower;
			}
		}
		return sum;
	}

	private static double getForHeatNet(Project project) {
		double f = project.costSettings.fundingHeatNet; // EUR/m
		if (f <= 0)
			return 0;
		return project.heatNet.length * f;
	}

	private static double getForTransferStations(Project project) {
		double f = project.costSettings.fundingTransferStations;
		if (f <= 0)
			return 0;
		double count = 0;
		for (Consumer c : project.consumers) {
			if (c.disabled)
				continue;
			if (c.transferStation != null) {
				count += 1.0;
				continue;
			}
			ProductCosts cost = c.transferStationCosts;
			if (cost != null && cost.investment > 0) {
				count += 1.0;
				continue;
			}
		}
		return f * count;
	}
}
