package sophena.calc;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import sophena.math.energetic.Producers;
import sophena.math.energetic.SeasonalItem;
import sophena.model.HoursTrace;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
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

		Map<Producer, SolarCalcState> solarCalcStates = new HashMap<Producer, SolarCalcState>();

		for(Producer producer: r.producers)
			if(producer.solarCollector != null & producer.solarCollectorSpec != null)
				solarCalcStates.put(producer, new SolarCalcState(solarCalcLog, project, producer));

		for (int hour = 0; hour < Stats.HOURS; hour++) {
			bufferCalcState.preStep(hour);
			
			if(hour == 01)
				r.bufferCapacity[hour] = bufferCalcState.CalcHTCapacity(false);

			double requiredLoad = r.loadCurve[hour];			
			double suppliedPower = 0;
		
			boolean haveAtLeastOneHTProducer = false;

			for (int k = 0; k < r.producers.length; k++) {
				Producer producer = r.producers[k];

				SolarCalcState solarCalcState = solarCalcStates.get(producer);
				if(solarCalcState != null)
					solarCalcState.calcPre(hour, bufferCalcState.TE, bufferCalcState.TV);
			}
			
			for (int k = 0; k < r.producers.length; k++) {
				if (requiredLoad <= 0)
					break;

				Producer producer = r.producers[k];
			
				SolarCalcState solarCalcState = solarCalcStates.get(producer);
				boolean isSolarProducer = solarCalcState != null;
				boolean isHTProducer = isProducerHT(producer, solarCalcState);

				// Check whether the collector is working for the current hour
				if(isSolarProducer && solarCalcState.getPhase() != SolarCalcPhase.Betrieb)
					continue;
			
				// Check whether the producer can be taken
				if (isInterrupted(k, hour, interruptions))
					continue;

				// Maximum amount of power currently needed for heatnet and buffer				
				double maxLoad = requiredLoad + (isHTProducer ?
					bufferCalcState.CalcHTCapacity(!isSolarProducer) :
					bufferCalcState.CalcNTCapacity(!isSolarProducer));
					
				double power = getSuppliedPower(producer, hour, solarCalcState, requiredLoad, maxLoad);
				
				// Don't use expensive peek load producer if the buffer has enaugh HT power left to satisfy the heatnet 
				if (bufferCalcState.totalUnloadableHTPower() > requiredLoad && producer.function == ProducerFunction.PEAK_LOAD)
					continue;
				
				// Always take solar power. If more then needed it will heat up the collector until stagnation
				if(!isSolarProducer && power > maxLoad)
					continue;

				if(isHTProducer)
				{
					// Mainly use HT power for the heatnet and leftover to charge the buffer 
					double surplus = power - requiredLoad;					
					if(surplus > 0)	
					{
						surplus = bufferCalcState.load(hour, surplus, isHTProducer, !isSolarProducer);					
						requiredLoad = 0;
						power -= surplus;
					}
					else
						requiredLoad -= power;					
				}
				else
				{
					// Mainly use NT power to charge the buffer, then add the rest to the heatnet in order to increase return temperature
					double surplus = bufferCalcState.load(hour, power, isHTProducer, !isSolarProducer);
					// Not sure if this is ok, because we need min. one HT producer for return temperature increasement 
					if(surplus < requiredLoad)
					{
						requiredLoad -= surplus;
						surplus = 0;
					}
					else
					{
						surplus -= requiredLoad;
						requiredLoad = 0;												
					}
					power -= surplus;
				}
				
				suppliedPower += power;					
				r.producerResults[k][hour] = power;

				// Write back used power in order to heat up the collector with the not used part
				if(isSolarProducer)
					solarCalcState.setConsumedPower(power * 1000);

				// Only if min. one HT producer is used at the current hour NT engery from the buffer can be unloaded in order to increase return temperature
				if(isHTProducer)
					haveAtLeastOneHTProducer = true;

				// Take the rest from buffer and do not use further producers if possible
				if(bufferCalcState.totalUnloadableHTPower() > requiredLoad) {
					break;
				}
			} // end producer loop
			
			for (int k = 0; k < r.producers.length; k++) {
				Producer producer = r.producers[k];

				SolarCalcState solarCalcState = solarCalcStates.get(producer);
				if(solarCalcState != null)
					solarCalcState.calcPost(hour);
			}

			if (requiredLoad >= 0) {
				
				double remainingRequiredLoad = bufferCalcState.unload(hour, requiredLoad, true);
				if(remainingRequiredLoad > 0 && haveAtLeastOneHTProducer)
					remainingRequiredLoad = bufferCalcState.unload(hour, remainingRequiredLoad, false);
				
				double bufferPower = requiredLoad - remainingRequiredLoad;

				suppliedPower += bufferPower;
				r.suppliedBufferHeat[hour] = bufferPower;
			}

			// buffer capacity with buffer loss
			r.bufferLoss[hour] = bufferCalcState.applyLoss(hour);

			r.suppliedPower[hour] = suppliedPower;

			if ((hour + 1) < Stats.HOURS) {
				r.bufferCapacity[hour + 1] = bufferCalcState.CalcHTCapacity(false);
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
	//	case OTHER_HEAT_SOURCE:
	//		return producer
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
