package sophena.calc;

import sophena.model.Project;

public class ProjectResult {

	public EnergyResult energyResult;
	public CostResult costResult;

	ProjectResult() {
	}

	public static ProjectResult calculate(Project project) {
		ProjectResult r = new ProjectResult();
		r.energyResult = EnergyCalculator.calculate(project);
		r.costResult = new CostCalculator(project, r.energyResult).calculate();
		return r;
	}

}
