package sophena.math.costs;

import sophena.calc.CostResultItem;
import sophena.model.Project;

public class Replacements {

	private Replacements() {
	}

	public static int getNumber(CostResultItem item, Project project) {
		if (item == null || item.costs == null || project == null)
			return 0;
		int usageDuration = item.costs.duration;
		int projectDuration = project.projectDuration;
		if (usageDuration >= projectDuration || usageDuration == 0)
			return 0;
		double pdur = (double) projectDuration;
		double udur = (double) usageDuration;
		double res = Math.ceil(pdur / udur) - 1.0;
		return (int) res;
	}

	public static double getCashValue(int replacement, CostResultItem item,
			Project project, double interestRate) {
		if (item == null)
			return 0;
		int usageDuration = item.costs.duration;
		if (usageDuration <= 0)
			return 0;
		if (project == null || project.costSettings == null)
			return 0;
		double ir = 1 + interestRate / 100;
		double priceChange = project.costSettings.investmentFactor;
		double year = replacement * usageDuration;
		return item.costs.investment * Math.pow(priceChange, year)
				/ Math.pow(ir, year);
	}

}
