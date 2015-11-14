package sophena.math.costs;

import sophena.calc.CostResultItem;
import sophena.model.Project;

public class CapitalCosts {

	private CapitalCosts() {
	}

	public double get(CostResultItem item, Project project, double interestRate) {
		if (item == null || item.costs == null || project == null)
			return 0;
		double annuityFactor = AnnuitiyFactor.get(project, interestRate);
		double residualValue = ResidualValue.get(item, project, interestRate);
		int projectDuration = project.projectDuration;
		int usageDuration = item.costs.duration;
		if (projectDuration <= usageDuration)
			return (item.costs.investment - residualValue) * annuityFactor;
		int replacements = Replacements.getNumber(item, project);
		double costs = item.costs.investment;
		for (int i = 1; i <= replacements; i++) {
			costs += Replacements.getCashValue(i, item, project, interestRate);
		}
		costs -= residualValue;
		return costs * annuityFactor;
	}
}
