package sophena.calc;

import sophena.model.Project;

public class ProjectResult {

	public EnergyResult energyResult;
	public CostResult costResult;
	public CostResult costResultFunding;

	ProjectResult() {
	}

	public static ProjectResult calculate(Project project) {
		ProjectResult r = new ProjectResult();
		r.energyResult = EnergyCalculator.calculate(project);
		CostCalculator costCalc = new CostCalculator(project, r.energyResult);
		costCalc.withFunding(false);
		r.costResult = costCalc.calculate();
		costCalc.withFunding(true);
		r.costResultFunding = costCalc.calculate();
		return r;
	}

}
