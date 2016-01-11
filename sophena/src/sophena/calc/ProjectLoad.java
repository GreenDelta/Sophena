package sophena.calc;

import java.time.MonthDay;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.math.Smoothing;
import sophena.model.Consumer;
import sophena.model.HeatNet;
import sophena.model.LoadProfile;
import sophena.model.Project;
import sophena.model.Stats;

public class ProjectLoad {

	private ProjectLoad() {
	}

	public static double getSimultanousMax(Project project) {
		if (project == null || project.heatNet == null)
			return 0;
		double max = getMax(project);
		double sim = project.heatNet.simultaneityFactor * max;
		return Math.ceil(sim);
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
			load += c.heatingLoad;
		}
		return Math.ceil(load);
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
		double[] data = Smoothing.means(dynamicData, Smoothing.getCount(project));
		Stats.add(staticData, data);
		double netLoad = getNetLoad(project.heatNet);
		Arrays.setAll(data, i -> data[i] + netLoad);
		applyInterruption(data, project.heatNet);
		return data;
	}

	private static double getNetLoad(HeatNet net) {
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
		if (curve == null || net == null || !net.withInterruption)
			return;
		int startIndex = getIndex(net.interruptionStart, true);
		int endIndex = getIndex(net.interruptionEnd, false);
		if (startIndex == -1 || endIndex == -1)
			return;
		if (startIndex < endIndex) {
			for (int i = startIndex; i <= endIndex; i++)
				curve[i] = 0;
		} else {
			for (int i = 0; i < Stats.HOURS; i++) {
				if (i > startIndex || i < endIndex)
					curve[i] = 0;
			}
		}
	}

	private static int getIndex(String monthDay, boolean beginOfDay) {
		if (monthDay == null)
			return -1;
		int[] daysInMonths = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
		try {
			MonthDay value = MonthDay.parse(monthDay);
			int monthIdx = value.getMonthValue() - 1;
			int hours = 0;
			for (int m = 0; m < monthIdx; m++)
				hours += daysInMonths[m] * 24; // hours in months before
			hours += (value.getDayOfMonth() - 1) * 24; // + hours in current
			// month
			hours = beginOfDay ? hours : hours + 24;
			return hours < Stats.HOURS ? hours : Stats.HOURS - 1;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(ProjectLoad.class);
			log.error("failed to parse MonthDay " + monthDay, e);
			return -1;
		}
	}
}
