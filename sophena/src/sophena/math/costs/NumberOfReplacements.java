package sophena.math.costs;

import sophena.calc.CostResultItem;
import sophena.model.Project;

public class NumberOfReplacements {

	private NumberOfReplacements() {
	}

	public static int get(CostResultItem item, Project project) {
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

}
