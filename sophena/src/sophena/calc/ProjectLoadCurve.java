package sophena.calc;

import java.util.Arrays;

import sophena.model.Consumer;
import sophena.model.HeatNet;
import sophena.model.Project;
import sophena.model.Stats;

public class ProjectLoadCurve {

	public static double[] get(Project project) {
		double[] curve = new double[Stats.HOURS];
		if (project == null)
			return curve;
		for (Consumer consumer : project.getConsumers()) {
			double[] c = ConsumerLoadCurve.calculate(consumer,
					project.getWeatherStation());
			for (int i = 0; i < c.length; i++)
				curve[i] += c[i];
		}
		double netLoad = getNetLoad(project);
		Arrays.setAll(curve, (i) -> curve[i] + netLoad);
		return curve;
	}

	public static double getNetLoad(Project project) {
		if (project == null || project.getHeatNet() == null)
			return 0;
		HeatNet net = project.getHeatNet();
		double netLoad = (net.getPowerLoss() * net.getLength()) / 1000;
		return netLoad;
	}
}
