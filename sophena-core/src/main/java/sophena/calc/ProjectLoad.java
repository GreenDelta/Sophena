package sophena.calc;

import java.util.Arrays;

import sophena.math.Smoothing;
import sophena.model.Consumer;
import sophena.model.HeatNet;
import sophena.model.HoursTrace;
import sophena.model.LoadProfile;
import sophena.model.Project;
import sophena.model.Stats;

public class ProjectLoad {

	private ProjectLoad() {
	}

	/**
	 * The maximum load of the project can be entered by the user. If no value
	 * is entered the value is calculated from the heat net and consumer data.
	 */
	public static double getMax(Project project) {
		if (project == null)
			return 0;
		HeatNet net = project.heatNet;
		if (net != null && net.maxLoad != null)
			return net.maxLoad;
		double load = getNetLoad(net);
		for (Consumer c : project.consumers) {
			if (c.disabled)
				continue;
			load += c.heatingLoad;
		}
		return Math.ceil(load);
	}

	/**
	 * Get the maximum load of the project taking a possible simultaneous factor
	 * into account.
	 */
	public static double getSimultaneousMax(Project project) {
		if (project == null)
			return 0;
		double max = ProjectLoad.getMax(project);
		if (project.heatNet == null)
			return max;
		return Math.ceil(max * project.heatNet.simultaneityFactor);
	}

	public static double[] getCurve(Project project) {
		double[] dynamicData = new double[Stats.HOURS];
		double[] staticData = new double[Stats.HOURS];
		if (project == null)
			return dynamicData;
		for (Consumer consumer : project.consumers) {
			if (consumer.disabled)
				continue;
			LoadProfile p = ConsumerLoadCurve.calculate(consumer,
					project.weatherStation);
			Stats.add(p.dynamicData, dynamicData);
			Stats.add(p.staticData, staticData);
		}
		double[] data = Smoothing.means(dynamicData,
				Smoothing.getCount(project));
		Stats.add(staticData, data);
		double netLoad = getNetLoad(project.heatNet);
		Arrays.setAll(data, i -> data[i] + netLoad);
		applyInterruption(data, project.heatNet);
		return data;
	}

	public static double getNetLoad(HeatNet net) {
		if (net == null)
			return 0;
		double netLoad = (net.powerLoss * net.length) / 1000;
		return netLoad;
	}

	public static double[] getNetLoadCurve(HeatNet net) {
		double[] curve = new double[Stats.HOURS];
		if (net == null)
			return curve;
		double load = getNetLoad(net);
		Arrays.setAll(curve, (i) -> load);
		applyInterruption(curve, net);
		return curve;
	}

	public static void applyInterruption(double[] curve, HeatNet net) {
		if (curve == null || net == null || net.interruption == null)
			return;
		int[] interval = HoursTrace.getDayInterval(net.interruption);
		HoursTrace.applyInterval(curve, interval, (old, i) -> 0.0);
	}
}
