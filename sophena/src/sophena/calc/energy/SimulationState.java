package sophena.calc.energy;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import sophena.math.energetic.Producers;
import sophena.math.energetic.SeasonalItem;
import sophena.model.HoursTrace;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.Project;
import sophena.model.Stats;
import sophena.model.TimeInterval;
import sophena.rcp.app.Workspace;
import sophena.utils.Temperature;

/**
 * Aggregates all mutable state of an energy simulation run:
 * the buffer state, per-producer solar collector states,
 * per-producer heat pump states, and the solar calculation log.
 * <p>
 * Once extracted from EnergyCalculator, it will be used as a single
 * state object whose methods replace the inline iteration loops.
 */
class SimulationState {

	private final Project project;

	final SolarLog solarLog;
	final BufferState bufferState;
	final Map<Producer, SolarState> solarStates;
	final Map<Producer, HeatPumpState> heatPumpStates;
	final Map<Producer, boolean[]> interruptions;

	SimulationState(Project project) {
		this.project = project;

		solarLog = new SolarLog();
		bufferState = new BufferState(project, solarLog);
		solarStates = new HashMap<>();
		heatPumpStates = new HashMap<>();
		interruptions = new HashMap<>();

		// initialize states for solar collectors and heat pumps
		for (var p : project.producers) {
			if (p.disabled)
				continue;
			if (p.solarCollector != null && p.solarCollectorSpec != null) {
				solarStates.put(p, new SolarState(solarLog, project, p));
			}
			if (p.heatPump != null) {
				heatPumpStates.put(p, new HeatPumpState(solarLog, project, p));
			}

			// add possible interruptions
			if (p.interruptions.isEmpty())
				continue;
			var breaks = new boolean[Stats.HOURS];
			for (TimeInterval time : p.interruptions) {
				int[] interval = HoursTrace.getHourInterval(time);
				HoursTrace.applyInterval(breaks, interval, (old, idx) -> true);
			}
			interruptions.put(p, breaks);
		}
	}

	void updateBefore(int hour) {
		bufferState.updateBefore(hour);
		for (var state : solarStates.values()) {
			state.updateBefore(hour, bufferState.getTE(), bufferState.getTV());
		}
		for (var state : heatPumpStates.values()) {
			state.updateBefore(hour, bufferState.getTR(), bufferState.getTV());
		}
	}

	void setConsumedPower(Producer producer, double power) {
		var solar = solarStates.get(producer);
		if (solar != null) {
			solar.setConsumedPower(power * 1000);
			return;
		}
		var pump = heatPumpStates.get(producer);
		if (pump != null) {
			pump.setConsumedPower(power * 1000);
		}
	}

	void updateAfter(int hour) {
		for (var state : solarStates.values()) {
			state.updateAfter(hour);
		}
		for (var state : heatPumpStates.values()) {
			state.updateAfter(hour);
		}
	}

	boolean hasHighTemperatureProducerAt(int hour) {
		for (var producer : project.producers) {
			var type = bufferLoadTypeOf(producer, hour);
			if (type == BufferLoadType.HIGH_TEMP)
				return true;
		}
		return false;
	}

	void collectResultsInto(EnergyResult r) {
		for (int k = 0; k < r.producers.length; k++) {
			var producer = r.producers[k];
			var solar = solarStates.get(producer);
			if (solar != null) {
				r.producerStagnationDays[k] = solar.numStagnationDays;
			}
			var pump = heatPumpStates.get(producer);
			if (pump != null) {
				r.producerJaz[k] = pump.getJAZ();
			}
		}
	}

	BufferLoadType bufferLoadTypeOf(Producer producer, int hour) {
		if (isDisabled(producer, hour))
			return BufferLoadType.NONE;

		// for producer profiles it is based on the temperature level
		if (producer.profile != null
			&& producer.profile.temperaturLevel != null) {
			double temp = producer.profile.temperaturLevel[hour];
			if (temp >= bufferState.getTMAX())
				return BufferLoadType.HIGH_TEMP;
			if (temp >= bufferState.getTV())
				return BufferLoadType.FLOW_TEMP;
			if (temp >= bufferState.getTR())
				return BufferLoadType.LOW_TEMP;
			return BufferLoadType.NONE;
		}

		// solar collectors
		var solar = solarStates.get(producer);
		if (solar != null) {
			return solar.isNotOperating()
				? BufferLoadType.NONE
				: Objects.requireNonNullElse(
				solar.getBufferLoadType(), BufferLoadType.NONE);
		}

		// heat pumps
		var pump = heatPumpStates.get(producer);
		if (pump != null)
			return Objects.requireNonNullElse(
				pump.getBufferLoadType(), BufferLoadType.NONE);

		// everything else should be a high temperature generator,
		// but we still check the product group
		if (producer.productGroup == null)
			return BufferLoadType.HIGH_TEMP;
		return switch (producer.productGroup.type) {
			// heat pumps or solar collectors without linked products
			// cannot be correctly handled
			case HEAT_PUMP, SOLAR_THERMAL_PLANT -> BufferLoadType.NONE;
			case null, default -> BufferLoadType.HIGH_TEMP;
		};
	}

	double getTargetTemperature(Producer producer, int hour) {
		var solar = solarStates.get(producer);
		if (solar != null)
			return solar.TK_i;
		var heatPump = heatPumpStates.get(producer);
		if (heatPump != null)
			return heatPump.getTK_i();
		return producer.profile != null && producer.profile.temperaturLevel != null
			? producer.profile.temperaturLevel[hour]
			: bufferState.getTV();
	}

	double minPower(Producer p, int hour) {
		if (p == null)
			return 0;
		if (p.profile != null)
			return Stats.get(p.profile.minPower, hour);
		return p.boiler != null
			? p.boiler.minPower * Producers.heatRecoveryFactor(p)
			: 0;
	}

	double maxPower(Producer p, int hour) {
		if (p == null)
			return 0;
		if (p.profile != null)
			return Stats.get(p.profile.maxPower, hour);

		var solar = solarStates.get(p);
		if (p.solarCollector != null && solar != null)
			return solar.getAvailablePowerInKWh();
		var pump = heatPumpStates.get(p);
		if (p.heatPump != null && pump != null)
			return pump.getMaxPower();

		return p.boiler != null
			? p.boiler.maxPower * Producers.heatRecoveryFactor(p)
			: 0;
	}

	double getSuppliedPower(
		Producer producer, int hour, double requiredLoad, double maxLoad
	) {
		double bMin = minPower(producer, hour);
		double bMax = maxPower(producer, hour);
		double load = producer.function == ProducerFunction.PEAK_LOAD
			? requiredLoad
			: maxLoad;
		return Math.clamp(load, bMin, bMax);
	}

	private boolean isDisabled(Producer producer, int hour) {
		if (producer == null || producer.disabled)
			return true;
		var breaks = interruptions.get(producer);
		if (breaks != null && breaks[hour])
			return true;

		if (!producer.isOutdoorTemperatureControl)
			return false;
		var temp = Temperature.of(project, hour);
		return switch (producer.outdoorTemperatureControlKind) {
			case From -> temp < producer.outdoorTemperature;
			case Until -> temp > producer.outdoorTemperature;
			case null -> true;
		};
	}

	void writeSolarLog() {
		if (solarStates.isEmpty())
			return;

		double[] chargeLevels = new double[Stats.HOURS];
		double[] flowTemp = new double[Stats.HOURS];
		double[] returnTemp = new double[Stats.HOURS];
		double minTemp = Temperature.minimumOf(project);
		double maxTemp = project.maxConsumerHeatTemperature();
		for (int hour = 0; hour < Stats.HOURS; hour++) {
			double temp = Temperature.of(project, hour);
			var item = SeasonalItem.calc(
				project.heatNet, hour, minTemp, maxTemp, temp);
			chargeLevels[hour] = item.targetChargeLevel;
			flowTemp[hour] = item.flowTemperature;
			returnTemp[hour] = item.returnTemperature;
		}

		try {
			var dir = new File(Workspace.dir(), "log");
			try (var pw = new PrintWriter(new File(dir, "SolarCalcLog.log"))) {
				pw.println(solarLog);
			}
			SolarLog.writeCsv(
				new File(dir, "seasonal_targetchargelevels.csv"), chargeLevels);
			SolarLog.writeCsv(
				new File(dir, "seasonal_TV.csv"), flowTemp);
			SolarLog.writeCsv(
				new File(dir.getAbsolutePath(), "seasonal_TR.csv"), returnTemp);
		} catch (Exception ignored) {
		}
	}
}
