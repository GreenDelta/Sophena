package sophena.math.costs;

import sophena.model.Project;

public class AnnuitiyFactor {

	public static double get(double interestFactor, int duration) {
		return (interestFactor - 1) / (1 - Math.pow(interestFactor, -duration));
	}

	public static double get(Project project, double interestRate) {
		if (project == null)
			return 0;
		double q = 1 + interestRate / 100;
		return get(q, project.projectDuration);
	}

}
