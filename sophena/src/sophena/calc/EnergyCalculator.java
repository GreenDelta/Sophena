package sophena.calc;

import sophena.math.energetic.Buffers;
import sophena.math.energetic.Producers;
import sophena.model.HoursTrace;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.Project;
import sophena.model.Stats;
import sophena.model.TimeInterval;

class EnergyCalculator {

	private Project project;

	private double maxBufferCapacity = 0;

	private EnergyCalculator(Project project) {
		this.project = project;
	}

	public static EnergyResult calculate(Project project) {
		return new EnergyCalculator(project).doIt();
	}

	private EnergyResult doIt() {

		maxBufferCapacity = Buffers.maxCapacity(project.heatNet);
		double bufferLossFactor = Buffers.lossFactor(project.heatNet);
		EnergyResult r = new EnergyResult(project);
		r.bufferCapacity[0] = maxBufferCapacity;
		boolean[][] interruptions = interruptions(r);

		boolean loadBuffer = false; // a flag for the summer mode

		for (int hour = 0; hour < Stats.HOURS; hour++) {

			double requiredLoad = r.loadCurve[hour];
			double bufferCapacity = r.bufferCapacity[hour];
			double maxLoad = requiredLoad + bufferCapacity;
			double bufferPotential = maxBufferCapacity - bufferCapacity;

			double suppliedPower = 0;

			boolean isSummerMode = r.producers.length > 0
					&& requiredLoad < Producers.minPower(r.producers[0], hour)
					&& !r.producers[0].hasProfile();
			if (isSummerMode && (bufferPotential / maxBufferCapacity) > 0.9) {
				// set the load `loadBuffer` flag to false when the buffer is
				// full; because of buffer loss it may is never really full so
				// we set some arbitrary cutoff for now.
				loadBuffer = false;
			}

			for (int k = 0; k < r.producers.length; k++) {
				if (requiredLoad <= 0)
					break;

				Producer producer = r.producers[k];

				if (bufferPotential >= requiredLoad) {
					// the required load can be fully taken from the buffer.
					// we do not take any producer when we are in summer mode.
					// and no peak load producer in this case
					if (isSummerMode && !loadBuffer)
						break;
					if (producer.function == ProducerFunction.PEAK_LOAD)
						continue;
				}

				if (isSummerMode) {
					loadBuffer = true;
					if (bufferCapacity < Producers.minPower(
							r.producers[0], hour)) {
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
				if (maxLoad < Producers.minPower(producer, hour))
					continue;

				double power = getSuppliedPower(producer, hour,
						requiredLoad, maxLoad);
				if (power > requiredLoad) {
					bufferCapacity -= (power - requiredLoad);
				}
				suppliedPower += power;
				maxLoad -= power;
				requiredLoad -= power;
				r.producerResults[k][hour] = power;

				if (bufferPotential >= requiredLoad) {
					// take rest from buffer
					break;
				}
			} // end producer loop

			if (requiredLoad >= 0 && bufferPotential > 0) {
				double bufferPower = requiredLoad;
				if (bufferPotential < requiredLoad) {
					bufferPower = bufferPotential;
				}
				suppliedPower += bufferPower;
				r.suppliedBufferHeat[hour] = bufferPower;
				bufferCapacity += bufferPower;
			}

			r.suppliedPower[hour] = suppliedPower;

			// buffer capacity with buffer loss
			bufferPotential = maxBufferCapacity - bufferCapacity;
			double bufferLoss = Buffers.loss(project.heatNet,
					bufferLossFactor, bufferPotential / maxBufferCapacity);
			r.bufferLoss[hour] = bufferLoss;
			bufferCapacity = bufferCapacity + bufferLoss;
			if (bufferCapacity > maxBufferCapacity) {
				bufferCapacity = maxBufferCapacity;
			}
			if ((hour + 1) < Stats.HOURS) {
				r.bufferCapacity[hour + 1] = bufferCapacity;
			}

		} // end hour loop

		calcTotals(r);

		return r;
	}

	private double getSuppliedPower(Producer producer, int hour,
			double requiredLoad, double maxLoad) {
		double bMin = Producers.minPower(producer, hour);
		double bMax = Producers.maxPower(producer, hour);
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
