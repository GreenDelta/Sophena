package sophena.calc.energy;

import java.io.File;
import java.util.HashMap;

import sophena.calc.biogas.BiogasPlants;
import sophena.math.energetic.SeasonalItem;
import sophena.model.HoursTrace;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.Project;
import sophena.model.Stats;
import sophena.model.TimeInterval;
import sophena.rcp.app.Workspace;
import sophena.utils.Temperature;

class EnergyCalculator {

	private final Project project;

	EnergyCalculator(Project project) {
		this.project = project;
	}

	EnergyResult calculate() {

		for (var producer : project.producers) {
			if (producer.biogasPlant != null) {
				BiogasPlants.syncProducerProfile(project, producer);
			}
		}

		var solarLog = new SolarLog();
		var bufferState = new BufferState(project, solarLog);

		var r = new EnergyResult(project);
		boolean[][] interruptions = interruptions(r);

		var solarStates = new HashMap<Producer, SolarState>();
		var heatPumpCalcStates = new HashMap<Producer, HeatPumpState>();

		for (var producer : r.producers) {
			if (producer.solarCollector != null & producer.solarCollectorSpec != null)
				solarStates.put(producer, new SolarState(solarLog, project, producer));

			if (producer.heatPump != null)
				heatPumpCalcStates.put(producer, new HeatPumpState(solarLog, project, producer));
		}

		for (int hour = 0; hour < Stats.HOURS; hour++) {
			bufferState.preStep(hour);

			if (hour == 0)
				r.bufferCapacity[hour] = bufferState.CalcHTCapacity(false);

			double requiredLoad = r.loadCurve[hour];
			double totalSuppliedPower = 0;
			double heatNetSuppliedPower = 0;

			for (int k = 0; k < r.producers.length; k++) {
				var producer = r.producers[k];

				var solarCalcState = solarStates.get(producer);
				if (solarCalcState != null)
					solarCalcState.calcPre(hour, bufferState.getTE(), bufferState.getTV());

				var heatPumpCalcState = heatPumpCalcStates.get(producer);
				if (heatPumpCalcState != null)
					heatPumpCalcState.calcPre(hour, bufferState.getTR(), bufferState.getTV());
			}

			double TL_i = Temperature.of(project, hour);

			// Determine if there is at least one HT producer
			boolean haveAtLeastOneHTProducer = false;
			for (int k = 0; k < r.producers.length; k++) {
				var producer = r.producers[k];

				var solarState = solarStates.get(producer);
				var heatPumpCalcState = heatPumpCalcStates.get(producer);
				boolean isSolarProducer = solarState != null;
				var bufferLoadType = getProducerBufferLoadType(producer, bufferState, solarState, heatPumpCalcState, hour);

				if (bufferLoadType == BufferLoadType.NONE)
					continue;

				// Check whether the collector is working for the current hour
				if (isSolarProducer && solarState.isNotOperating())
					continue;

				// Check whether the producer can be taken
				if (isInterrupted(k, hour, interruptions))
					continue;

				if (isDisabledByOutdoorTemp(producer, TL_i))
					continue;

				if (bufferLoadType == BufferLoadType.HIGH_TEMP)
					haveAtLeastOneHTProducer = true;
			}

			// Main producer loop
			for (int k = 0; k < r.producers.length; k++) {
				requiredLoad = (r.loadCurve[hour] - heatNetSuppliedPower);
				if (requiredLoad <= 0)
					break;

				var producer = r.producers[k];

				var solarState = solarStates.get(producer);
				var heatPumpState = heatPumpCalcStates.get(producer);
				boolean isSolarProducer = solarState != null;
				var bufferLoadType = getProducerBufferLoadType(producer, bufferState, solarState, heatPumpState, hour);

				if (bufferLoadType == BufferLoadType.NONE)
					continue;

				// Check whether the collector is working for the current hour
				if (isSolarProducer && solarState.isNotOperating())
					continue;

				// Check whether the producer can be taken
				if (isInterrupted(k, hour, interruptions))
					continue;

				if (isDisabledByOutdoorTemp(producer, TL_i))
					continue;

				double TR = bufferState.getTR();
				double TV = bufferState.getTV();

				double TK_i = TV;
				if (isSolarProducer)
					TK_i = solarState.TK_i;
				if (heatPumpState != null)
					TK_i = heatPumpState.getTK_i();
				if (producer.profile != null && producer.profile.temperaturLevel != null)
					TK_i = producer.profile.temperaturLevel[hour];

				// For NT producer calculate the power factor based on their temperature level
				double loadFactorTK_i = (bufferLoadType != BufferLoadType.LOW_TEMP) ? 1 : (TK_i - TR) / (TV - TR);
				double reducedLoad = Math.max(0, r.loadCurve[hour] * loadFactorTK_i - heatNetSuppliedPower);
				double bufferNTUnloadLimit = Math.max(0, r.loadCurve[hour] * bufferState.getNTLoadFactor(false) - heatNetSuppliedPower);

				// Amount of power currently needed for heatnet and buffer based on producer buffer load type
				double maxLoadRel = reducedLoad + (bufferLoadType == BufferLoadType.HIGH_TEMP ?
					bufferState.CalcHTCapacity(producer.function != ProducerFunction.MAX_LOAD) :
					bufferState.CalcNTCapacity(producer.function != ProducerFunction.MAX_LOAD, loadFactorTK_i));

				// Maximum amount of power currently needed for heatnet and buffer
				double maxLoadAbs = reducedLoad + (bufferLoadType == BufferLoadType.HIGH_TEMP ?
					bufferState.CalcHTCapacity(false) :
					bufferState.CalcNTCapacity(false, loadFactorTK_i));

				// Power which can be provided by the producer
				double power = getSuppliedPower(producer, hour, solarState, heatPumpState, reducedLoad, maxLoadRel);
				double unloadableNTPower = Math.min(bufferNTUnloadLimit, bufferState.totalUnloadableNTPower());

				if (!isSolarProducer) {
					double unloadablePower = bufferState.totalUnloadableHTPower() + bufferState.totalUnloadableVTPower() + (haveAtLeastOneHTProducer ? unloadableNTPower : 0);
					// Don't use expensive peek load producer if the buffer has enough HT, VT and NT power left to satisfy the heatnet
					if ((unloadablePower > requiredLoad) && producer.function == ProducerFunction.PEAK_LOAD)
						continue;

					// Don't use base load producer if buffer is still above base load limit after required unload to satisfy the heatnet
					if (bufferState.getMaxTargetLoadStillReachedAfterPartialUnload(requiredLoad) && (unloadablePower > requiredLoad) && producer.function == ProducerFunction.BASE_LOAD)
						continue;
				}

				// Don't limit producer unless they exceed the maximum power currently needed, allways use solar producer
				if (power <= maxLoadAbs) {
					if (bufferLoadType == BufferLoadType.HIGH_TEMP && producer.function == ProducerFunction.PEAK_LOAD && bufferState.totalUnloadableNTPower() > 0) {
						double p = Math.min(unloadableNTPower, power - Util.minPower(producer, hour));
						bufferState.unload(hour, p, BufferLoadType.LOW_TEMP);
						totalSuppliedPower += p;
						heatNetSuppliedPower += p;
						power -= p;
						r.suppliedBufferHeat[hour] += p;
						reducedLoad -= p;
					}

					if (haveAtLeastOneHTProducer || bufferLoadType != BufferLoadType.LOW_TEMP) {
						// Mainly use producer power for the heatnet and leftover to charge the buffer
						double surplus = power - reducedLoad;
						heatNetSuppliedPower += surplus > 0 ? power - surplus : power;
						if (surplus > 0)
							power -= bufferState.load(hour, surplus, bufferLoadType, false, loadFactorTK_i);
					} else
						power = 0;

					totalSuppliedPower += power;
					r.producerResults[k][hour] = power;
				}

				// Write back used power in order to heat up the collector with the not used part
				if (isSolarProducer)
					solarState.setConsumedPower(power * 1000);

				if (heatPumpState != null)
					heatPumpState.setConsumedPower(power * 1000);
			}
			// end producer loop

			for (int k = 0; k < r.producers.length; k++) {
				var producer = r.producers[k];

				var solarCalcState = solarStates.get(producer);
				if (solarCalcState != null)
					solarCalcState.calcPost(hour);

				var heatPumpCalcState = heatPumpCalcStates.get(producer);
				if (heatPumpCalcState != null)
					heatPumpCalcState.calcPost(hour);
			}

			requiredLoad = (r.loadCurve[hour] - heatNetSuppliedPower);
			if (requiredLoad >= 0) {

				double bufferNTUnloadLimit = Math.max(0, r.loadCurve[hour] * bufferState.getNTLoadFactor(false) - heatNetSuppliedPower);

				double remainingRequiredLoad = bufferState.unload(hour, requiredLoad, BufferLoadType.HIGH_TEMP);

				if (remainingRequiredLoad > 0)
					remainingRequiredLoad = bufferState.unload(hour, remainingRequiredLoad, BufferLoadType.FLOW_TEMP);

				if (remainingRequiredLoad > 0 && haveAtLeastOneHTProducer) {
					double p = remainingRequiredLoad - Math.min(bufferNTUnloadLimit, remainingRequiredLoad);
					remainingRequiredLoad = bufferState.unload(hour, Math.min(bufferNTUnloadLimit, remainingRequiredLoad), BufferLoadType.LOW_TEMP) + p;
				}

				double bufferPower = requiredLoad - remainingRequiredLoad;
				totalSuppliedPower += bufferPower;
				r.suppliedBufferHeat[hour] += bufferPower;
			}

			// buffer capacity with buffer loss
			r.bufferLoss[hour] = bufferState.applyLoss(hour);

			r.suppliedPower[hour] = totalSuppliedPower;

			if ((hour + 1) < Stats.HOURS) {
				r.bufferCapacity[hour + 1] = bufferState.CalcHTCapacity(false);
			}

			bufferState.postStep(hour);

		} // end hour loop

		for (int k = 0; k < r.producers.length; k++) {
			var producer = r.producers[k];

			var solarCalcState = solarStates.get(producer);
			if (solarCalcState != null)
				r.producerStagnationDays[k] = solarCalcState.numStagnationDays;

			var heatPumpCalcState = heatPumpCalcStates.get(producer);
			if (heatPumpCalcState != null)
				r.producerJaz[k] = heatPumpCalcState.getJAZ();
		}


		double[] targetChargeLevels = new double[Stats.HOURS];
		double[] flowTemperatures = new double[Stats.HOURS];
		double[] returnTemperatures = new double[Stats.HOURS];
		double minWeatherStationTemperature = Temperature.minimumOf(project);
		double maxConsumerHeatingLimit = project.maxConsumerHeatTemperature();
		for (int hour = 0; hour < Stats.HOURS; hour++) {
			double temperature = Temperature.of(project, hour);
			var item = SeasonalItem.calc(project.heatNet, hour, minWeatherStationTemperature, maxConsumerHeatingLimit, temperature);

			targetChargeLevels[hour] = item.targetChargeLevel;
			flowTemperatures[hour] = item.flowTemperature;
			returnTemperatures[hour] = item.returnTemperature;
		}

		try {
			var logDir = new File(Workspace.dir(), "log");
			var filename = logDir.getAbsolutePath() + "SolarCalcLog.log";
			try (java.io.PrintWriter pw = new java.io.PrintWriter(filename)) {
				pw.println(solarLog);
			}

			SolarLog.writeCsv(logDir.getAbsolutePath() + "seasonal_targetchargelevels.csv", targetChargeLevels);
			SolarLog.writeCsv(logDir.getAbsolutePath() + "seasonal_TV.csv", flowTemperatures);
			SolarLog.writeCsv(logDir.getAbsolutePath() + "seasonal_TR.csv", returnTemperatures);
		} catch (java.io.FileNotFoundException ignored) {
		}

		calcTotals(r);

		r.fermenterHeatDemand = 0;
		for (var p : r.producers) {
			if (p.biogasPlant == null)
				continue;
			double[] demand = BiogasPlants.heatDemandOf(project, p.biogasPlant);
			r.fermenterHeatDemand += Stats.sum(demand);
		}

		return r;
	}

	private boolean isDisabledByOutdoorTemp(Producer producer, double temp) {
		if (!producer.isOutdoorTemperatureControl)
			return false;
		return switch (producer.outdoorTemperatureControlKind) {
			case From -> temp < producer.outdoorTemperature;
			case Until -> temp > producer.outdoorTemperature;
			case null -> true;
		};
	}

	private BufferLoadType getProducerBufferLoadType(
		Producer producer,
		BufferState bufferCalcState,
		SolarState solarCalcState,
		HeatPumpState heatPumpCalcState,
		int hour
	) {
		if (producer.profile != null && producer.profile.temperaturLevel != null) {
			double temp = producer.profile.temperaturLevel[hour];
			if (temp >= bufferCalcState.getTMAX())
				return BufferLoadType.HIGH_TEMP;
			if (temp >= bufferCalcState.getTV())
				return BufferLoadType.FLOW_TEMP;
			if (temp >= bufferCalcState.getTR())
				return BufferLoadType.LOW_TEMP;
			return BufferLoadType.NONE;
		}

		if (producer.productGroup == null)
			return BufferLoadType.HIGH_TEMP;
		return switch (producer.productGroup.type) {
			case HEAT_PUMP -> heatPumpCalcState.getBufferLoadType();
			case SOLAR_THERMAL_PLANT -> solarCalcState.getBufferLoadType();
			case null, default -> BufferLoadType.HIGH_TEMP;
		};
	}

	private double getSuppliedPower(Producer producer, int hour, SolarState solarCalcState,
	                                HeatPumpState heatPumpCalcState,
	                                double requiredLoad, double maxLoad) {
		double bMin = Util.minPower(producer, hour);
		double bMax = Util.maxPower(producer, solarCalcState, heatPumpCalcState, hour);
		double load = producer.function == ProducerFunction.PEAK_LOAD
			? requiredLoad
			: maxLoad;
		return Math.min(Math.max(load, bMin), bMax);
	}

	private void calcTotals(EnergyResult r) {
		r.totalLoad = Stats.sum(r.loadCurve);
		for (int i = 0; i < r.producers.length; i++) {
			Producer p = r.producers[i];
			double total = Stats.sum(r.producerResults[i]);
			r.totalHeats.put(p.id, total);
			r.totalProducedHeat += total;
		}
		r.totalBufferedHeat = Stats.sum(r.suppliedBufferHeat);
		r.totalBufferLoss = Stats.sum(r.bufferLoss);
	}

	private boolean[][] interruptions(EnergyResult r) {
		boolean[][] interruptions = new boolean[r.producers.length][];
		for (int i = 0; i < r.producers.length; i++) {
			Producer p = r.producers[i];
			if (p == null || p.interruptions.isEmpty())
				continue;
			boolean[] interruption = new boolean[Stats.HOURS];
			for (TimeInterval time : p.interruptions) {
				int[] interval = HoursTrace.getHourInterval(time);
				HoursTrace.applyInterval(interruption, interval, (old, idx) -> true);
			}
			interruptions[i] = interruption;
		}
		return interruptions;
	}

	private boolean isInterrupted(
		int producer, int hour, boolean[][] interruptions
	) {
		boolean[] interruption = interruptions[producer];
		if (interruption == null)
			return false;
		return interruption[hour];
	}
}
