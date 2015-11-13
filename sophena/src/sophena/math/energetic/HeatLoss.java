package sophena.math.energetic;

import sophena.calc.EnergyResult;
import sophena.calc.ProjectLoad;
import sophena.model.Project;
import sophena.model.Stats;

public class HeatLoss {

	public static double getAbsoluteConversionLossKWh(EnergyResult result) {
		if (result == null)
			return 0;
		double fuelEnergy = FuelEnergyDemand.getTotalKWh(result);
		double heat = result.totalProducedHeat;
		return fuelEnergy - heat;
	}

	public static double getRelativeConversionLoss(EnergyResult result) {
		if (result == null)
			return 0;
		double fuelEnergy = FuelEnergyDemand.getTotalKWh(result);
		if (fuelEnergy == 0)
			return 0;
		double loss = getAbsoluteConversionLossKWh(result);
		return loss / fuelEnergy;
	}

	public static double getAbsoluteNetLossKWh(Project project) {
		if (project == null || project.heatNet == null)
			return 0;
		double[] loadCurve = ProjectLoad.getNetLoadCurve(project.heatNet);
		double totalLoss = Stats.sum(loadCurve);
		return totalLoss;
	}

	public static double getRelativeNetLoss(Project project, EnergyResult result) {
		if (project == null || result == null || result.totalProducedHeat == 0)
			return 0;
		double abs = getAbsoluteNetLossKWh(project);
		return abs / result.totalProducedHeat;
	}

	public static double getAbsoluteTotalLossKWh(Project project, EnergyResult result) {
		return getAbsoluteConversionLossKWh(result)
				+ getAbsoluteNetLossKWh(project);
	}

	public static double getRelativeTotalLoss(Project project, EnergyResult result) {
		double fuelEnergy = FuelEnergyDemand.getTotalKWh(result);
		double loss = getAbsoluteTotalLossKWh(project, result);
		return loss / fuelEnergy;
	}

}
