package sophena.calc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import sophena.math.energetic.Producers;
import sophena.math.energetic.SeasonalItem;
import sophena.model.HoursTrace;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.ProductType;
import sophena.model.Project;
import sophena.model.Stats;
import sophena.model.TimeInterval;
import sophena.rcp.Workspace;

class EnergyCalculator {

	private Project project;

	private EnergyCalculator(Project project) {
		this.project = project;
	}

	public static EnergyResult calculate(Project project, CalcLog log) {
		return new EnergyCalculator(project).doIt(log);
	}

	private EnergyResult doIt(CalcLog log) {
		SolarCalcLog solarCalcLog = new SolarCalcLog();
		var bufferCalcState = new BufferCalcState(project, solarCalcLog);

		EnergyResult r = new EnergyResult(project);
		boolean[][] interruptions = interruptions(r);

		boolean loadBuffer = false; // a flag for the summer mode
		
		Map<Producer, SolarCalcState> solarCalcStates = new HashMap<Producer, SolarCalcState>();

		for(Producer producer: r.producers)
			if(producer.solarCollector != null & producer.solarCollectorSpec != null)
				solarCalcStates.put(producer, new SolarCalcState(solarCalcLog, project, producer));

		for (int hour = 0; hour < Stats.HOURS; hour++) {
			bufferCalcState.preStep(hour);
			
			if(hour == 01)
				r.bufferCapacity[hour] = bufferCalcState.CalcHTCapacity();

			double requiredLoad = r.loadCurve[hour];
			double maxLoad = requiredLoad + r.bufferCapacity[hour];
			
			double suppliedPower = 0;

			boolean isSummerMode = r.producers.length > 0
					&& requiredLoad < Producers.minPower(r.producers[0], solarCalcStates.get(r.producers[0]), hour)
					&& !r.producers[0].hasProfile();
			if (isSummerMode && bufferCalcState.getLoadFactor() > 0.9) {
				// set the load `loadBuffer` flag to false when the buffer is
				// full; because of buffer loss it may is never really full so
				// we set some arbitrary cutoff for now.
				loadBuffer = false;
			}

			for (int k = 0; k < r.producers.length; k++) {
				Producer producer = r.producers[k];

				SolarCalcState solarCalcState = solarCalcStates.get(producer);
				if(solarCalcState != null)
					solarCalcState.calcPre(hour, bufferCalcState.TE);
			}
			
			for (int k = 0; k < r.producers.length; k++) {
				if (requiredLoad <= 0)
					break;

				Producer producer = r.producers[k];

				if (bufferCalcState.totalUnloadablePower() >= requiredLoad) {
					// the required load can be fully taken from the buffer.
					// we do not take any producer when we are in summer mode.
					// and no peak load producer in this case
					if (isSummerMode && !loadBuffer)
						break;
					if (producer.function == ProducerFunction.PEAK_LOAD)
						continue;
				}

				SolarCalcState solarCalcState = solarCalcStates.get(producer);
				if(solarCalcState != null && solarCalcState.getPhase() != SolarCalcPhase.Betrieb)
					continue;

				if (isSummerMode) {
					loadBuffer = true;
					var bufferCapacity = isProducerHT(producer, solarCalcState)
							? bufferCalcState.CalcHTCapacity()
							: bufferCalcState.CalcNTCapacity();
					if (bufferCapacity < Producers.minPower(
							r.producers[0], solarCalcState, hour)) {
						// set buffer loading to false when the minimal power of
						// the producer with rank=1 is lower than the capacity
						// of the buffer; this also avoids buffer loading with
						// secondary producers in the summer mode
						loadBuffer = false;
					}
				}

				// check whether the producer can be taken
				if (isInterrupted(k, hour, interruptions))
					continue;
				if (maxLoad < Producers.minPower(producer, solarCalcState, hour))
					continue;

				double power = getSuppliedPower(producer, hour, solarCalcState,
						requiredLoad, maxLoad);
				if (power > requiredLoad) {
					var surplus = power - requiredLoad;
					
					boolean isHT = isProducerHT(producer, solarCalcState);
					bufferCalcState.load(hour, surplus, isHT);
				}
				suppliedPower += power;
				maxLoad -= power;
				requiredLoad -= power;
				r.producerResults[k][hour] = power;

				if(solarCalcState != null)
					solarCalcState.setConsumedPower(power * 1000);

				if(bufferCalcState.totalUnloadablePower() > requiredLoad) {
					// take rest from buffer
					break;
				}
			} // end producer loop
			
			for (int k = 0; k < r.producers.length; k++) {
				Producer producer = r.producers[k];

				SolarCalcState solarCalcState = solarCalcStates.get(producer);
				if(solarCalcState != null)
					solarCalcState.calcPost(hour);
			}

			// buffer capacity with buffer loss
			r.bufferLoss[hour] = bufferCalcState.applyLoss(hour);

			if (requiredLoad >= 0) {
				
				double remainingRequiredLoad = bufferCalcState.unload(hour, requiredLoad, false);
				if(remainingRequiredLoad > 0)
					remainingRequiredLoad = bufferCalcState.unload(hour, remainingRequiredLoad, true);
				
				double bufferPower = requiredLoad - remainingRequiredLoad;

				suppliedPower += bufferPower;
				r.suppliedBufferHeat[hour] = bufferPower;
			}

			r.suppliedPower[hour] = suppliedPower;

			if ((hour + 1) < Stats.HOURS) {
				r.bufferCapacity[hour + 1] = bufferCalcState.CalcHTCapacity();
			}
			
			bufferCalcState.postStep(hour);

		} // end hour loop

		for (int k = 0; k < r.producers.length; k++) {
			Producer producer = r.producers[k];

			SolarCalcState solarCalcState = solarCalcStates.get(producer);
			if(solarCalcState != null)
				r.producerStagnationDays[k] = solarCalcState.getNumStagnationDays();
		}

		
		double[] targetChargeLevels = new double[Stats.HOURS];
		double[] flowTemperatures = new double[Stats.HOURS];
		double[] returnTemperatures = new double[Stats.HOURS];
		for (int hour = 0; hour < Stats.HOURS; hour++) {
			var item = SeasonalItem.calc(project.heatNet, hour);
			
			targetChargeLevels[hour] = item.targetChargeLevel;
			flowTemperatures[hour] = item.flowTemperature;
			returnTemperatures[hour] = item.returnTemperature;
		}
		
		try {
			var logDir = new File(Workspace.dir(), "log");
			var filename = logDir.getAbsolutePath() + "SolarCalcLog.log";
			try(java.io.PrintWriter pw = new java.io.PrintWriter(filename))
			{
				pw.println(solarCalcLog.toString());
			}

			SolarCalcLog.writeCsv(logDir.getAbsolutePath() + "seasonal_targetchargelevels.csv", targetChargeLevels);
			SolarCalcLog.writeCsv(logDir.getAbsolutePath() + "seasonal_TV.csv", flowTemperatures);
			SolarCalcLog.writeCsv(logDir.getAbsolutePath() + "seasonal_TR.csv", returnTemperatures);
}
		catch(java.io.FileNotFoundException err)
		{
		}

		calcTotals(r);

		return r;
	}
	
	private boolean isProducerHT(Producer producer, SolarCalcState solarCalcState)
	{
		switch(producer.productGroup.type)
		{
		case HEAT_PUMP:
			return false; //TODO
		case SOLAR_THERMAL_PLANT:
			return solarCalcState.getOperationMode() == SolarCalcOperationMode.TargetTemperature;
		//TODO: Abwärme (Erzeugerlastgang)
		default:
			return true;
		}
	}

	private double getSuppliedPower(Producer producer, int hour, SolarCalcState solarCalcState,
			double requiredLoad, double maxLoad) {
		double bMin = Producers.minPower(producer, solarCalcState, hour);
		double bMax = Producers.maxPower(producer, solarCalcState, hour);
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
				HoursTrace.applyInterval(interruption, interval, (old, idx) -> {
					return true;
				});
			}
			interruptions[i] = interruption;
		}
		return interruptions;
	}

	private boolean isInterrupted(int producer, int hour,
			boolean[][] interruptions) {
		boolean[] interruption = interruptions[producer];
		if (interruption == null)
			return false;
		return interruption[hour];
	}
}
