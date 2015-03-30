package sophena.calc;

import sophena.db.Database;
import sophena.model.Consumer;
import sophena.model.Project;

public class ProjectLoadCurve {

	// TODO: this is currently just a test implementation

	public static double[] calulate(Project project, Database db) {
		double[] curve = new double[8760];
		if(project == null)
			return curve;
		for(Consumer consumer : project.getConsumers()){
			double[] c = ConsumerLoadCurve.calculate(consumer,
					project.getWeatherStation());
			for(int i = 0; i < c.length; i++)
				curve[i] += c[i];
		}
		return curve;
	}
}
