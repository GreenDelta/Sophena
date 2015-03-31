package sophena.calc;

import sophena.model.Consumer;
import sophena.model.Project;
import sophena.model.Stats;

public class ProjectLoadCurve {

	// TODO: this is currently just a test implementation

	public static double[] calulate(Project project) {
		double[] curve = new double[Stats.HOURS];
		if (project == null)
			return curve;
		for (Consumer consumer : project.getConsumers()) {
			double[] c = ConsumerLoadCurve.calculate(consumer,
					project.getWeatherStation());
			for (int i = 0; i < c.length; i++)
				curve[i] += c[i];
		}
		return curve;
	}
}
