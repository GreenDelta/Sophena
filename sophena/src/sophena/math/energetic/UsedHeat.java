package sophena.math.energetic;

import sophena.calc.ProjectResult;

public class UsedHeat {

	private UsedHeat() {
	}

	public static double get(ProjectResult result) {
		if (result == null || result.energyResult == null)
			return 0;
		var r = result.energyResult;
		return r.totalProducedHeat
				- r.heatNetLoss
				- r.totalBufferLoss;
	}
}
