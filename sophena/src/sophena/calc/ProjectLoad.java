package sophena.calc;

import java.util.Arrays;

import sophena.math.Smoothing;
import sophena.model.Consumer;
import sophena.model.HeatNet;
import sophena.model.HoursTrace;
import sophena.model.Project;
import sophena.model.Stats;

public class ProjectLoad {

	private ProjectLoad() {
	}

	/**
	 * The maximum load of the project can be entered by the user. If no value is
	 * entered the value is calculated from the heat net and consumer data.
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

	/**
	 * Calculates the load curve of the project and applies smoothing based on the
	 * simultaneity and smoothing factors of the project on the dynamic part of the
	 * load.
	 */
	public static double[] getSmoothedCurve(Project project) {
		double[] dynamicData = new double[Stats.HOURS];
		double[] staticData = new double[Stats.HOURS];
		if (project == null)
			return dynamicData;
		for (var consumer : project.consumers) {
			if (consumer.disabled)
				continue;
			var profile = ConsumerLoadCurve.calculate(
					consumer, project.weatherStation);
			Stats.add(profile.dynamicData, dynamicData);
			Stats.add(profile.staticData, staticData);
		}
		double[] data = Smoothing.on(dynamicData,
				Smoothing.getCount(project));
		Stats.add(staticData, data);
		double netLoad = getNetLoad(project.heatNet);
		Arrays.setAll(data, i -> data[i] + netLoad);
		applyInterruption(data, project.heatNet);
		return data;
	}

	/**
	 * Calculates the load curve of the project without applying smoothing on the
	 * dynamic part of the data.
	 */
	public static double[] getRawCurve(Project project) {
		var data = new double[Stats.HOURS];
		if (project == null)
			return data;
		for (var consumer : project.consumers) {
			if (consumer.disabled)
				continue;
			var profile = ConsumerLoadCurve.calculate(
					consumer, project.weatherStation);
			Stats.add(profile.dynamicData, data);
			Stats.add(profile.staticData, data);
		}
		double netLoad = getNetLoad(project.heatNet);
		Arrays.setAll(data, i -> data[i] + netLoad);
		applyInterruption(data, project.heatNet);
		return data;
	}

	public static double getNetLoad(HeatNet net) {
		return net == null
				? 0
				: net.powerLoss * net.length / 1000.0;
	}

	public static double[] getNetLoadCurve(HeatNet net) {
		double[] curve = new double[Stats.HOURS];
		if (net == null)
			return curve;
		double load = getNetLoad(net);
		Arrays.fill(curve, load);
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
