package sophena.calc;

import java.util.List;

import org.openlca.commons.Res;

import sophena.model.Project;

public class Comparison {

	public final Project[] projects;
	public final ProjectResult[] results;

	/// Calculates the results of the given projects. Returns an error when the
	/// calculation of a project fails.
	public static Res<Comparison> calculate(List<Project> list) {
		Project[] projects = new Project[list.size()];
		ProjectResult[] results = new ProjectResult[list.size()];
		for (int i = 0; i < list.size(); i++) {
			Project p = list.get(i);
			projects[i] = p;
			var r = ProjectResult.calculate(p);
			if (r.isError())
				return r.wrapError("Das Projekt \"" + p.name
					+ "\" konnte nicht berechnet werden.");
			results[i] = r.value();
		}
		return Res.ok(new Comparison(projects, results));
	}

	private Comparison(Project[] projects, ProjectResult[] results) {
		this.projects = projects;
		this.results = results;
	}

}
