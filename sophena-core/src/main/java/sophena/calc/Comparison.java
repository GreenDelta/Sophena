package sophena.calc;

import java.util.List;

import sophena.model.Project;

public class Comparison {

	public final Project[] projects;
	public final ProjectResult[] results;

	public static Comparison calculate(List<Project> list) {
		Project[] projects = new Project[list.size()];
		ProjectResult[] results = new ProjectResult[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Project p = list.get(i);
			projects[i] = p;
			ProjectResult r = ProjectResult.calculate(p);
			results[i] = r;
		}
		return new Comparison(projects, results);
	}

	private Comparison(Project[] projects, ProjectResult[] results) {
		this.projects = projects;
		this.results = results;
	}

}
