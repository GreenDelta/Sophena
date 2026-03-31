package sophena.rcp.navigation.actions.producers;

import sophena.calc.ProjectLoad;
import sophena.model.HoursTrace;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.Stats;

class PeakDemandEstimator {

	private final Project project;

	private PeakDemandEstimator(Project project) {
		this.project = project;
	}

	static double estimate(Project project) {
		return project != null
			? new PeakDemandEstimator(project).estimate()
			: 0;
	}

	private double estimate() {
		var curve = ProjectLoad.getSmoothedCurve(project);
		double maxDiff = 0;
		for (int hour = 0; hour < Stats.HOURS; hour++) {
			double load = Stats.get(curve, hour);
			double provided = 0;
			for (var producer : project.producers) {
				provided += powerOf(producer, hour);
			}
			double diff = Math.max(0, load - provided);
			maxDiff = Math.max(maxDiff, diff);
		}
		return Math.ceil(maxDiff);
	}

	private double powerOf(Producer producer, int hour) {
		if (producer == null
			|| producer.disabled
			|| producer.solarCollector != null
			|| isInterrupted(producer, hour)
			|| isDisabledByOutdoorTemperature(producer, hour)) {
			return 0;
		}

		if (producer.profile != null)
			return Stats.get(producer.profile.minPower, hour);
		if (producer.heatPump != null)
			return Math.max(0, producer.heatPump.ratedPower);
		if (producer.boiler != null)
			return Math.max(0, producer.boiler.maxPower);
		return 0;
	}

	private boolean isInterrupted(Producer producer, int hour) {
		if (producer == null)
			return false;
		for (var interruption : producer.interruptions) {
			var interval = HoursTrace.getDayInterval(interruption);
			if (interval.length < 2) continue;
			int start = interval[0];
			int end = interval[1];
			if (start > end) continue;
			if (hour >= start && hour <= end) {
				return true;
			}
		}
		return false;
	}

	/// Returns whether the producer is unavailable in the given hour because its
	/// outdoor-temperature control disables it.
	private boolean isDisabledByOutdoorTemperature(Producer producer, int hour) {
		if (!producer.isOutdoorTemperatureControl)
			return false;
		if (project.weatherStation == null || project.weatherStation.data == null)
			return true;
		double temp = Stats.get(project.weatherStation.data, hour);
		return switch (producer.outdoorTemperatureControlKind) {
			case From -> temp < producer.outdoorTemperature;
			case Until -> temp > producer.outdoorTemperature;
			case null -> false;
		};
	}
}
