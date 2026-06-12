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

}
