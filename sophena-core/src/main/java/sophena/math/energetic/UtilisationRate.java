package sophena.math.energetic;

import java.util.ArrayList;
import java.util.List;

import sophena.Defaults;
import sophena.calc.EnergyResult;
import sophena.model.HoursTrace;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.Stats;
import sophena.model.TimeInterval;

/**
 * Calculates the utilisation rate of a boiler. The calculated utilisation rate
 * is at least 0.01 (1%).
 */
public class UtilisationRate {

	public static double get(double efficiencyRate, int fullLoadHours) {
		return get(efficiencyRate, fullLoadHours, Stats.HOURS);
	}

	public static double get(double efficiencyRate, int fullLoadHours,
			int usageDuration) {
		if (fullLoadHours == 0)
			return 0.01;
		double ud = usageDuration;
		double fh = fullLoadHours;
		double standbyRate = 1
				/ ((ud / fh - 1) * Defaults.SPECIFIC_STAND_BY_LOSS + 1);
		double ur = standbyRate * efficiencyRate;
		return ur > 0.01 ? ur : 0.01;
	}

	public static double get(Project project, Producer producer,
			EnergyResult result) {
		if (producer == null || result == null)
			return 0.01;
		if (producer.utilisationRate != null)
			return producer.utilisationRate;
		double generatedHeat = result.totalHeat(producer);
		int fullLoadHours = (int) Producers.fullLoadHours(producer,
				generatedHeat);
		int usageDuration = getUsageHours(project, producer);
		double er = Producers.efficiencyRate(producer);
		return get(er, fullLoadHours, usageDuration);
	}

	/**
	 * Calculate the usage hours of the given producer in the project. These are
	 * simply the annual hours reduced by hours of interruptions of the heating
	 * net or the producer. Note that the interruption times may overlap and
	 * double counting has to be avoided.
	 */
	public static int getUsageHours(Project project, Producer producer) {
		int hours = Stats.HOURS;
		if (project == null || producer == null)
			return hours;
		int[] netInterruption = null;
		if (project.heatNet != null && project.heatNet.interruption != null) {
			netInterruption = HoursTrace
					.getDayInterval(project.heatNet.interruption);
		}
		if (netInterruption == null && producer.interruptions.isEmpty())
			return hours;
		List<int[]> interruptions = new ArrayList<>();
		if (netInterruption != null) {
			interruptions.add(netInterruption);
		}
		for (TimeInterval interval : producer.interruptions) {
			int[] interruption = HoursTrace.getDayInterval(interval);
			if (interruption != null) {
				interruptions.add(interruption);
			}
		}
		boolean[] interrupted = new boolean[Stats.HOURS];
		for (int[] interruption : interruptions) {
			HoursTrace.applyInterval(interrupted, interruption,
					(old, i) -> true);
		}
		for (int i = 0; i < Stats.HOURS; i++) {
			if (interrupted[i]) {
				hours--;
			}
		}
		return hours > 0 ? hours : 0;
	}
}
