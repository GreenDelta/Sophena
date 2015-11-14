package sophena.math.costs;

import sophena.model.CostSettings;
import sophena.model.Project;

class TestProject {

	static Project create() {
		Project project = new Project();
		project.projectDuration = 20;
		CostSettings settings = new CostSettings();
		project.costSettings = settings;
		settings.interestRate = 2;
		settings.interestRateFunding = 1.5;
		settings.investmentFactor = 1.015;
		return project;
	}

}
