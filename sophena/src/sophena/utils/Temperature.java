package sophena.utils;

import sophena.model.Project;
import sophena.model.Stats;

public class Temperature {

	private Temperature() {
	}

	public static double of(Project project, int hour) {
		return project != null && project.weatherStation != null
			? Stats.get(project.weatherStation.data, hour)
			: 0;
	}

	/// Returns the minimum outdoor temperature of the given project.
	public static double minimumOf(Project project) {
		return project != null && project.weatherStation != null
			? Stats.min(project.weatherStation.data)
			: 0;
	}
}
