package sophena.calc.load;

import java.util.Arrays;

import sophena.calc.biogas.FermenterHeatDemand;
import sophena.calc.specs.SeasonalItem;
import sophena.model.HeatNet;
import sophena.model.HoursTrace;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.Stats;
import sophena.utils.Temperature;

/// Calculates the load (heat demand) of a project. The load of a project
/// consists of the following parts:
///
/// - the load of the consumers, which has a dynamic (temperature dependent,
///   space heating) and a static (e.g. hot water) part, see
///   `ConsumerLoadCurve`
/// - the heat losses of the heat net (`getNetLoadCurve`)
/// - the heat demand for heating the fermenters of biogas plants that are
///   used as producers in the project (`getFermenterHeatDemand`)
///
/// The interruption of the heat net (e.g. in summer) sets all parts of the load
/// to 0 in the interrupted period. The heat demand of the fermenters is part of
/// the load curve of the energy simulation (`getSmoothedCurve`, and its
/// dynamic part `getDynamicCurve`) and of the required producer power
/// (`getSimultaneousMax`), but not of the maximum load of the consumers and the
/// heat net (`getMax`).
public class ProjectLoad {

	private ProjectLoad() {
	}

	/// Returns the maximum load of the project in kW without the simultaneity
	/// factor. It can be entered by the user (`HeatNet.maxLoad`); otherwise it
	/// is the sum of the heating loads of the consumers plus the maximum heat
	/// loss of the heat net. The heat demand of the fermenters of biogas plants
	/// is not included (see `getSimultaneousMax`).
	public static double getMax(Project project) {
		if (project == null)
			return 0;
		HeatNet net = project.heatNet;
		if (net != null && net.maxLoad != null)
			return net.maxLoad;

		double load = getMaxNetLoad(project);
		for (var c : project.consumers) {
			if (c.disabled)
				continue;
			load += c.heatingLoad;
		}
		return Math.ceil(load);
	}

	/// Returns the power in kW that the producers of the project have to
	/// provide: the maximum load of the project (`getMax`) multiplied by the
	/// simultaneity factor of the heat net plus the maximum heat demand of the
	/// fermenters of biogas plants. It is compared with the installed power of
	/// the producers, e.g. to detect a power deficit. Because biogas plants
	/// count with their full thermal power on the side of the installed power,
	/// the heat demand of their fermenters must be added to the demand side.
	/// The simultaneity factor is not applied to the fermenter demand.
	public static double getSimultaneousMax(Project project) {
		if (project == null)
			return 0;
		double max = getMax(project);
		if (project.heatNet != null) {
			max *= project.heatNet.simultaneityFactor;
		}
		double fermenterMax = Stats.max(getFermenterHeatDemand(project));
		return Math.ceil(max + fermenterMax);
	}

	/// Calculates the hourly load curve of the project in kW that the producers
	/// have to cover in the energy simulation (`EnergyResult.loadCurve`). It is
	/// the sum of:
	///
	/// - the dynamic load of the consumers, smoothed according to the
	///   simultaneity and smoothing factors of the heat net (see `Smoothing`)
	/// - the static load of the consumers (not smoothed)
	/// - the heat losses of the heat net
	/// - the heat demand of the fermenters of biogas plants (not smoothed)
	///
	/// The interruption of the heat net is applied to all parts. The curve is
	/// the sum of `getDynamicCurve` and `getStaticCurve`.
	public static double[] getSmoothedCurve(Project project) {
		double[] data = getDynamicCurve(project);
		Stats.add(getStaticCurve(project), data);
		return data;
	}

	/// Returns the dynamic part of the load curve in kW: the smoothed dynamic
	/// load of the consumers, the heat losses of the heat net, and the heat
	/// demand of the fermenters of biogas plants (which is also temperature
	/// dependent), with the interruption of the heat net applied. Together with
	/// `getStaticCurve`, this is the load curve of the energy simulation
	/// (`getSmoothedCurve`); it is used for the chart "Jahresdauerlinie -
	/// Netzlast" in the results.
	public static double[] getDynamicCurve(Project project) {
		double[] dynamicData = new double[Stats.HOURS];
		if (project == null)
			return dynamicData;
		for (var consumer : project.consumers) {
			if (consumer.disabled)
				continue;
			var profile = ConsumerLoadCurve.calculate(
				consumer, project.weatherStation);
			Stats.add(profile.dynamicData, dynamicData);
		}
		double[] data = Smoothing.on(project, dynamicData);
		double[] netLoad = getNetLoadCurve(project);
		Arrays.setAll(data, i -> data[i] + netLoad[i]);
		applyInterruption(data, project.heatNet);
		Stats.add(getFermenterHeatDemand(project), data);
		return data;
	}

	/// Returns the static load of the consumers in kW (e.g. for hot water),
	/// with the interruption of the heat net applied. See `getDynamicCurve`.
	public static double[] getStaticCurve(Project project) {
		double[] staticData = new double[Stats.HOURS];
		if (project == null)
			return staticData;
		for (var consumer : project.consumers) {
			if (consumer.disabled)
				continue;
			var profile = ConsumerLoadCurve.calculate(
				consumer, project.weatherStation);
			Stats.add(profile.staticData, staticData);
		}
		applyInterruption(staticData, project.heatNet);
		return staticData;
	}

	/// Returns the maximum hourly heat loss of the heat net in kW, see
	/// `getNetLoadCurve`.
	public static double getMaxNetLoad(Project project) {
		if (project == null)
			return 0;

		double[] netLoadCurve = getNetLoadCurve(project);
		double max = 0;
		for (int hour = 0; hour < Stats.HOURS; hour++) {
			if (netLoadCurve[hour] > max)
				max = netLoadCurve[hour];
		}
		return max;
	}

	/// Calculates the hourly heat losses of the heat net in kW from the heat
	/// loss coefficient of the net (`HeatNet.powerLoss` in W/K) and the
	/// difference between the mean net temperature and the soil temperature.
	/// The interruption of the heat net is applied.
	public static double[] getNetLoadCurve(Project project) {
		double[] curve = new double[Stats.HOURS];
		if (project == null)
			return curve;
		HeatNet net = project.heatNet;
		if (net == null || net.powerLoss <= 0)
			return curve;

		if (project.weatherStation == null
			|| project.weatherStation.data == null) {
			// flat curve when no there are no weather data
			// assuming ~50 K in temperature difference between
			// net and soil: (K * W/k) / 1000 -> kW * 1h
			Arrays.fill(curve, 50 * net.powerLoss / 1000.0);
			applyInterruption(curve, net);
			return curve;
		}

		// net.powerLoss is the total heat loss coefficient in W/K (see HeatNets).
		// The hourly heat loss is:
		//   (avgPipeTemp - 10 K) * powerLoss  [W]  → / 1000 → [kW]
		// where 10°C is the assumed soil temperature.
		//
		// When seasonal driving style is active, TV/TR vary per hour via
		// SeasonalItem; otherwise they are constant from HeatNet fields.
		var temperature = project.weatherStation.data;
		double min = Temperature.minimumOf(project);
		double max = project.maxConsumerHeatTemperature();
		for (int hour = 0; hour < temperature.length; hour++) {
			var item = SeasonalItem.calc(net, hour, min, max, temperature[hour]);
			double netTemp = (item.flowTemperature + item.returnTemperature) / 2;
			curve[hour] = (netTemp - 10.0) * net.powerLoss / 1000;
		}
		applyInterruption(curve, net);
		return curve;
	}

	/// Calculates the hourly heat demand in kW for heating the fermenters of
	/// the biogas plant of the given producer. Returns `null` when the producer
	/// is disabled or has no biogas plant.
	///
	/// In the project, the fermenters are heated with heat from the heat net:
	/// the demand is part of the load curve that the producers have to cover
	/// (see `getDynamicCurve`), and the same demand is subtracted from the heat
	/// of the biogas plant as it is consumed internally and not sold to the
	/// heat net (see `EnergyResult.fermenterHeatOf`). Both use this method, so
	/// that they always use the same values.
	///
	/// The interruption of the heat net is also applied to the fermenter
	/// demand: while the heat net is interrupted, the biogas plant heats its
	/// fermenters directly with the heat of its CHP and not via the heat net,
	/// so this heat is not part of the project.
	public static double[] getFermenterHeatDemand(
		Project project, Producer producer
	) {
		if (project == null
			|| producer == null
			|| producer.disabled
			|| producer.biogasPlant == null)
			return null;
		double[] demand = FermenterHeatDemand.of(project, producer.biogasPlant);
		applyInterruption(demand, project.heatNet);
		return demand;
	}

	/// Returns the hourly heat demand in kW for heating the fermenters of all
	/// biogas plants of the project, see `getFermenterHeatDemand(Project,
	/// Producer)`.
	private static double[] getFermenterHeatDemand(Project project) {
		var sum = new double[Stats.HOURS];
		if (project == null)
			return sum;
		for (var producer : project.producers) {
			var demand = getFermenterHeatDemand(project, producer);
			if (demand != null) {
				Stats.add(demand, sum);
			}
		}
		return sum;
	}

	/// Sets the values of the given curve to 0 in the period in which the heat
	/// net is interrupted (e.g. a summer shutdown).
	public static void applyInterruption(double[] curve, HeatNet net) {
		if (curve == null || net == null || net.interruption == null)
			return;
		int[] interval = HoursTrace.getDayInterval(net.interruption);
		HoursTrace.applyInterval(curve, interval, (_, _) -> 0.0);
	}
}
