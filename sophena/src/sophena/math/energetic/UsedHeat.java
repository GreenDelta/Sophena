package sophena.math.energetic;

import sophena.calc.ProjectResult;

public class UsedHeat {

	private UsedHeat() {
	}

	public static double get(ProjectResult result) {
		if (result == null || result.energyResult == null)
			return 0;
		double producedHeat = result.energyResult.totalProducedHeat;
		double distributionLoss = result.energyResult.heatNetLoss;
		return producedHeat - distributionLoss;
	}
}
