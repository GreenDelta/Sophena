package sophena.math.costs;

import sophena.calc.CostResultItem;
import sophena.model.Project;

public class ResidualValue {

	private ResidualValue() {
	}

	public static double get(CostResultItem item, Project project,
			double interestRate) {
		if (item == null || item.costs == null)
			return 0;
		int usageDuration = item.costs.duration;
		if (usageDuration < 1)
			return 0;
		if (project == null || project.costSettings == null)
			return 0;
		double ir = 1 + interestRate / 100;
		double priceChange = project.costSettings.investmentFactor;
		int replacements = Replacements.getNumber(item, project);
		double projectDuration = project.duration;
		return item.costs.investment
				* Math.pow(priceChange, replacements * usageDuration)
				* (((replacements + 1) * usageDuration - projectDuration)
						/ usageDuration)
				* (1 / Math.pow(ir, projectDuration));
	}

}
