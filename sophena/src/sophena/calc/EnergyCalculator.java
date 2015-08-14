package sophena.calc;

import sophena.model.Boiler;
import sophena.model.HeatNet;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.Project;
import sophena.model.Stats;

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
		maxBufferCapacity = calcMaxBufferCapacity();
		EnergyResult r = new EnergyResult(project);
		r.bufferCapacity[0] = 0.5 * maxBufferCapacity;
		for (int i = 0; i < Stats.HOURS; i++) {

			double requiredLoad = r.loadCurve[i];
			double bufferCapacity = r.bufferCapacity[i];
			double maxLoad = requiredLoad + bufferCapacity;
			double bufferPotential = maxBufferCapacity - bufferCapacity;

			double suppliedPower = 0;
			for (int k = 0; k < r.producers.length; k++) {
				if (requiredLoad <= 0)
					break;

				Producer producer = r.producers[k];
				Boiler boiler = producer.getBoiler();
				if (maxLoad < boiler.getMinPower())
					continue;

				double power = getSuppliedPower(requiredLoad, maxLoad,
						producer);
				if (power > requiredLoad) {
					bufferCapacity -= (power - requiredLoad);
				}
				suppliedPower += power;
				maxLoad -= power;
				requiredLoad -= power;
				r.producerResults[k][i] = power;

				if (bufferPotential >= requiredLoad) {
					// take rest from buffer
					break;
				}
			}

			if (requiredLoad >= 0 && bufferPotential > 0) {
				double bufferPower = requiredLoad;
				if (bufferPotential < requiredLoad)
					bufferPower = bufferPotential;
				suppliedPower += bufferPower;
				r.suppliedBufferHeat[i] = bufferPower;
				bufferCapacity += bufferPower;
			}
			if ((i + 1) < Stats.HOURS)
				r.bufferCapacity[i + 1] = bufferCapacity;
			r.suppliedPower[i] = suppliedPower;
		}

		calcTotals(r);

		return r;
	}

	private double getSuppliedPower(double requiredLoad, double maxLoad,
			Producer producer) {
		Boiler boiler = producer.getBoiler();
		double bMin = boiler.getMinPower();
		double bMax = boiler.getMaxPower();
		double load = producer.getFunction() == ProducerFunction.PEAK_LOAD
				? requiredLoad : maxLoad;
		return Math.min(Math.max(load, bMin), bMax);
	}

	private double calcMaxBufferCapacity() {
		HeatNet net = project.getHeatNet();
		if (net == null)
			return 0;
		double liters = net.bufferTankVolume;
		double deltaT = net.supplyTemperature - net.returnTemperature;
		double c = 1.166; // Wh/(kg K)
		return (c * liters * deltaT) / 1000;
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
	}
}
