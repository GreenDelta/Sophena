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
		if (project == null)
			return 0;
		else
			return getNetLoad(project.getHeatNet());
	}

	public static double getNetLoad(HeatNet net) {
		if (net == null)
			return 0;
		double netLoad = (net.getPowerLoss() * net.getLength()) / 1000;
		return netLoad;
	}
}
