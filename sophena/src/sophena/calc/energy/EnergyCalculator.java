package sophena.calc.energy;

import sophena.calc.biogas.BiogasPlants;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.Project;
import sophena.model.Stats;

class EnergyCalculator {

	private final Project project;
	private final SimulationState state;
	private final EnergyResult r;

	EnergyCalculator(Project project) {
		this.project = project;
		this.r = new EnergyResult(project);
		this.state = new SimulationState(project);
	}

	EnergyResult calculate() {
		syncBiogasProfiles();
		for (int hour = 0; hour < Stats.HOURS; hour++) {
			simulateHour(hour);
		}
		finalizeSimulation(r);
		return r;
	}

	private void syncBiogasProfiles() {
		for (var producer : project.producers) {
			if (producer.biogasPlant != null) {
				BiogasPlants.syncProducerProfile(project, producer);
			}
		}
	}

	private void simulateHour(int hour) {
		state.updateBefore(hour);

		var bufferState = state.bufferState;
		if (hour == 0) {
			r.bufferCapacity[hour] = bufferState.calcHTCapacity(false);
		}
		double totalSuppliedPower = 0;
		double heatNetSuppliedPower = 0;
		boolean hasHighTempProducer = state.hasHighTemperatureProducerAt(hour);

		for (int k = 0; k < r.producers.length; k++) {
			double requiredLoad = (r.loadCurve[hour] - heatNetSuppliedPower);
			if (requiredLoad <= 0)
				break;

			var producer = r.producers[k];
			var loadType = state.bufferLoadTypeOf(producer, hour);
			if (loadType == null || loadType == BufferLoadType.NONE)
				continue;


			double loadFactor = loadFactor(hour, producer, loadType);
			double reducedLoad = Math.max(0, r.loadCurve[hour] * loadFactor - heatNetSuppliedPower);
			double bufferNTUnloadLimit = Math.max(0, r.loadCurve[hour] * bufferState.getNTLoadFactor() - heatNetSuppliedPower);

			// Amount of power currently needed for heatnet and buffer based on producer buffer load type
			double maxLoadRel = reducedLoad + (loadType == BufferLoadType.HIGH_TEMP ?
				bufferState.calcHTCapacity(producer.function != ProducerFunction.MAX_LOAD) :
				bufferState.CalcNTCapacity(producer.function != ProducerFunction.MAX_LOAD, loadFactor));

			// Maximum amount of power currently needed for heatnet and buffer
			double maxLoadAbs = reducedLoad + (loadType == BufferLoadType.HIGH_TEMP ?
				bufferState.calcHTCapacity(false) :
				bufferState.CalcNTCapacity(false, loadFactor));

			// Power which can be provided by the producer
			double power = state.getSuppliedPower(producer, hour, reducedLoad, maxLoadRel);
			double unloadableNTPower = Math.min(bufferNTUnloadLimit, bufferState.totalUnloadableNTPower());

			if (producer.solarCollector == null) {
				double unloadablePower = bufferState.totalUnloadableHTPower() + bufferState.totalUnloadableVTPower() + (hasHighTempProducer ? unloadableNTPower : 0);
				// Don't use expensive peek load producer if the buffer has enough HT, VT and NT power left to satisfy the heatnet
				if ((unloadablePower > requiredLoad) && producer.function == ProducerFunction.PEAK_LOAD)
					continue;

				// Don't use base load producer if buffer is still above base load limit after required unload to satisfy the heatnet
				if (bufferState.getMaxTargetLoadStillReachedAfterPartialUnload(requiredLoad) && (unloadablePower > requiredLoad) && producer.function == ProducerFunction.BASE_LOAD)
					continue;
			}

			// Don't limit producer unless they exceed the maximum power currently needed, allways use solar producer
			if (power <= maxLoadAbs) {
				if (loadType == BufferLoadType.HIGH_TEMP && producer.function == ProducerFunction.PEAK_LOAD && bufferState.totalUnloadableNTPower() > 0) {
					double p = Math.min(unloadableNTPower, power - state.minPower(producer, hour));
					bufferState.unload(hour, p, BufferLoadType.LOW_TEMP);
					totalSuppliedPower += p;
					heatNetSuppliedPower += p;
					power -= p;
					r.suppliedBufferHeat[hour] += p;
					reducedLoad -= p;
				}

				if (hasHighTempProducer || loadType != BufferLoadType.LOW_TEMP) {
					// Mainly use producer power for the heatnet and leftover to charge the buffer
					double surplus = power - reducedLoad;
					heatNetSuppliedPower += surplus > 0 ? power - surplus : power;
					if (surplus > 0)
						power -= bufferState.load(hour, surplus, loadType, false, loadFactor);
				} else
					power = 0;

				totalSuppliedPower += power;
				r.producerResults[k][hour] = power;
			}

			state.setConsumedPower(producer, power);

		}
		// end producer loop

		state.updateAfter(hour);

		double requiredLoad = (r.loadCurve[hour] - heatNetSuppliedPower);
		if (requiredLoad >= 0) {

			double bufferNTUnloadLimit = Math.max(0, r.loadCurve[hour] * bufferState.getNTLoadFactor() - heatNetSuppliedPower);

			double remainingRequiredLoad = bufferState.unload(hour, requiredLoad, BufferLoadType.HIGH_TEMP);

			if (remainingRequiredLoad > 0)
				remainingRequiredLoad = bufferState.unload(hour, remainingRequiredLoad, BufferLoadType.FLOW_TEMP);

			if (remainingRequiredLoad > 0 && hasHighTempProducer) {
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
			r.bufferCapacity[hour + 1] = bufferState.calcHTCapacity(false);
		}

		bufferState.postStep(hour);
	}

	private double loadFactor(int hour, Producer producer, BufferLoadType type) {
		if (type != BufferLoadType.LOW_TEMP)
			return 1;
		// for low temperature producers, calculate the load factor based
		// on their temperature level
		var s = state.bufferState;
		double returnTemp = s.getTR();
		double flowTemp = s.getTV();
		double producerTemp = state.getTargetTemperature(producer, hour);
		return (producerTemp - returnTemp) / (flowTemp - returnTemp);
	}

	private void finalizeSimulation(EnergyResult r) {
		state.collectResultsInto(r);
		state.writeSolarLog();
		r.totalLoad = Stats.sum(r.loadCurve);
		for (int i = 0; i < r.producers.length; i++) {
			Producer p = r.producers[i];
			double total = Stats.sum(r.producerResults[i]);
			r.totalHeats.put(p.id, total);
			r.totalProducedHeat += total;
		}
		r.totalBufferedHeat = Stats.sum(r.suppliedBufferHeat);
		r.totalBufferLoss = Stats.sum(r.bufferLoss);

		r.fermenterHeatDemand = 0;
		for (var p : r.producers) {
			if (p.biogasPlant == null)
				continue;
			double[] demand = BiogasPlants.heatDemandOf(project, p.biogasPlant);
			r.fermenterHeatDemand += Stats.sum(demand);
		}
	}
}
