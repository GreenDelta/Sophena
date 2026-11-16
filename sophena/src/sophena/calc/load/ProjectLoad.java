package sophena.calc.load;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import sophena.calc.biogas.FermenterHeatDemand;
import sophena.calc.specs.SeasonalItem;
import sophena.model.HeatNet;
import sophena.model.HoursTrace;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.Stats;
import sophena.utils.Temperature;

/// The load (heat demand) of a project that the producers have to cover in the
/// energy simulation. It is calculated once with `ProjectLoad.of(project)` and
/// is part of the energy result, so that the calculations and the result views
/// use exactly the load with which the simulation was done. The load of a
/// project consists of the following parts:
///
/// - the load of the consumers, which has a dynamic (temperature dependent,
///   space heating) and a static (e.g. hot water) part, see
///   `ConsumerLoadCurve`; the dynamic part is smoothed according to the
///   simultaneity and smoothing factors of the heat net (see `Smoothing`)
/// - the heat losses of the heat net (`getNetLoadCurve`)
/// - the heat demand for heating the fermenters of biogas plants that are
///   used as producers in the project (see `fermenterDemands`)
///
/// The interruption of the heat net (e.g. in summer) sets all parts of the load
/// to 0 in the interrupted period. The arrays of this record are shared and
/// must not be modified.
///
/// @param dynamicCurve the hourly dynamic load in kW: the smoothed dynamic load
///        of the consumers, the heat losses of the heat net, and the heat
///        demand of the fermenters (which is also temperature dependent)
/// @param staticCurve the hourly static load of the consumers in kW
/// @param netLossCurve the hourly heat losses of the heat net in kW
/// @param fermenterDemands the hourly heat demand in kW for heating the
///        fermenters of the biogas plant of a producer, mapped by the ID of
///        the producer; only contains enabled producers with a biogas plant
/// @param simultaneousMax the power in kW that the producers have to provide,
///        see `simultaneousMaxOf`
public record ProjectLoad(
	double[] dynamicCurve,
	double[] staticCurve,
	double[] netLossCurve,
	Map<String, double[]> fermenterDemands,
	double simultaneousMax
) {

	/// Calculates the load of the given project. This is the only place where
	/// the fermenter simulation of the biogas plants runs for the load.
	public static ProjectLoad of(Project project) {
		if (project == null)
			return new ProjectLoad(
				new double[Stats.HOURS],
				new double[Stats.HOURS],
				new double[Stats.HOURS],
				Map.of(),
				0);

		double[] consumerDynamic = new double[Stats.HOURS];
		double[] staticCurve = new double[Stats.HOURS];
		for (var consumer : project.consumers) {
			if (consumer.disabled)
				continue;
			var profile = ConsumerLoadCurve.calculate(
				consumer, project.weatherStation);
			Stats.add(profile.dynamicData, consumerDynamic);
			Stats.add(profile.staticData, staticCurve);
		}
		applyInterruption(staticCurve, project.heatNet);

		double[] netLossCurve = getNetLoadCurve(project);
		var fermenterDemands = fermenterDemandsOf(project);

		// only the dynamic load of the consumers is smoothed; the net losses
		// and the fermenter demand are added after the smoothing
		double[] dynamicCurve = Smoothing.on(project, consumerDynamic);
		applyInterruption(dynamicCurve, project.heatNet);
		Stats.add(netLossCurve, dynamicCurve);
		for (var demand : fermenterDemands.values()) {
			Stats.add(demand, dynamicCurve);
		}

		return new ProjectLoad(
			dynamicCurve,
			staticCurve,
			netLossCurve,
			fermenterDemands,
			simultaneousMaxOf(project, fermenterDemands));
	}

	/// Calculates the hourly heat demand in kW for heating the fermenters of
	/// the biogas plants of the enabled producers of the project.
	///
	/// In the project, the fermenters are heated with heat from the heat net:
	/// the demand is part of the load curve that the producers have to cover,
	/// and the same demand is subtracted from the heat of the biogas plant as
	/// it is consumed internally and not sold to the heat net (see
	/// `fermenterHeatOf`).
	///
	/// The interruption of the heat net is also applied to the fermenter
	/// demand: while the heat net is interrupted, the biogas plant heats its
	/// fermenters directly with the heat of its CHP and not via the heat net,
	/// so this heat is not part of the project.
	private static Map<String, double[]> fermenterDemandsOf(Project project) {
		var demands = new HashMap<String, double[]>();
		for (var producer : project.producers) {
			if (producer.disabled || producer.biogasPlant == null)
				continue;
			double[] demand = FermenterHeatDemand.of(
				project, producer.biogasPlant);
			applyInterruption(demand, project.heatNet);
			demands.put(producer.id, demand);
		}
		return demands;
	}

	/// The power in kW that the producers of the project have to provide: the
	/// maximum load of the project (`getMax`) multiplied by the simultaneity
	/// factor of the heat net plus the maximum heat demand of the fermenters
	/// of biogas plants. It is compared with the installed power of the
	/// producers, e.g. to detect a power deficit. Because biogas plants count
	/// with their full thermal power on the side of the installed power, the
	/// heat demand of their fermenters must be added to the demand side. The
	/// simultaneity factor is not applied to the fermenter demand.
	private static double simultaneousMaxOf(
		Project project, Map<String, double[]> fermenterDemands
	) {
		double max = getMax(project);
		if (project.heatNet != null) {
			max *= project.heatNet.simultaneityFactor;
		}
		double[] fermenter = new double[Stats.HOURS];
		for (var demand : fermenterDemands.values()) {
			Stats.add(demand, fermenter);
		}
		return Math.ceil(max + Stats.max(fermenter));
	}

	/// Returns the hourly load curve of the project in kW that the producers
	/// have to cover in the energy simulation: the sum of the dynamic and
	/// static curve. Returns a new array.
	public double[] totalCurve() {
		double[] total = Arrays.copyOf(dynamicCurve, Stats.HOURS);
		Stats.add(staticCurve, total);
		return total;
	}

	/// The total load of the project in kWh.
	public double totalLoad() {
		return Stats.sum(totalCurve());
	}

	/// The total heat loss of the heat net in kWh.
	public double heatNetLoss() {
		return Stats.sum(netLossCurve);
	}

	/// The maximum hourly heat loss of the heat net in kW.
	public double maxNetLoss() {
		return Math.max(0, Stats.max(netLossCurve));
	}

	/// The heat in kWh that the fermenters of the biogas plant of the given
	/// producer need while the heat net is in operation. Returns 0 if the
	/// producer has no biogas plant.
	public double fermenterHeatOf(Producer producer) {
		if (producer == null || producer.id == null)
			return 0;
		double[] demand = fermenterDemands.get(producer.id);
		return demand != null ? Stats.sum(demand) : 0;
	}

	/// The total heat in kWh that the fermenters of all biogas plants of the
	/// project need while the heat net is in operation.
	public double totalFermenterHeat() {
		double sum = 0;
		for (var demand : fermenterDemands.values()) {
			sum += Stats.sum(demand);
		}
		return sum;
	}

	// region static utility methods

	/// Returns the maximum load of the project in kW without the simultaneity
	/// factor. It can be entered by the user (`HeatNet.maxLoad`); otherwise it
	/// is the sum of the heating loads of the consumers plus the maximum heat
	/// loss of the heat net. The heat demand of the fermenters of biogas plants
	/// is not included (see `simultaneousMax`).
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

	/// Returns the maximum hourly heat loss of the heat net in kW, see
	/// `getNetLoadCurve`.
	public static double getMaxNetLoad(Project project) {
		if (project == null)
			return 0;
		return Math.max(0, Stats.max(getNetLoadCurve(project)));
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

	/// Sets the values of the given curve to 0 in the period in which the heat
	/// net is interrupted (e.g. a summer shutdown).
	public static void applyInterruption(double[] curve, HeatNet net) {
		if (curve == null || net == null || net.interruption == null)
			return;
		int[] interval = HoursTrace.getDayInterval(net.interruption);
		HoursTrace.applyInterval(curve, interval, (_, _) -> 0.0);
	}

	// endregion
}
