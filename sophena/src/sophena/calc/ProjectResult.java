package sophena.calc;

import sophena.model.Project;

public class ProjectResult {

	public EnergyResult energyResult;
	public CostResult costResult;

	ProjectResult() {
	}

	public static ProjectResult calculate(Project project) {
		ProjectResult result = new ProjectResult();
		result.energyResult = EnergyCalculator.calculate(project);
		result.costResult = new CostResult();
		return result;
	}

}
