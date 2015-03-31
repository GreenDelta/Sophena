package sophena.calc;

import sophena.model.Boiler;
import sophena.model.HeatNet;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.Project;
import sophena.model.Stats;

public class ProjectCalculator {

	private Project project;

	private double maxBufferCapacity = 0;

	private ProjectCalculator(Project project) {
		this.project = project;
	}

	public static ProjectResult calculate(Project project) {
		return new ProjectCalculator(project).doIt();
	}

	private ProjectResult doIt() {
		maxBufferCapacity = calcMaxBufferCapacity();
		ProjectResult r = new ProjectResult(project);
		r.printHeader();
		for (int i = 0; i < Stats.HOURS; i++) {

			double requiredLoad = r.loadCurve[i];
			double bufferCapacity = getBufferCapacity(r, i);
			double maxLoad = requiredLoad + bufferCapacity;
			double bufferPotential = maxBufferCapacity - bufferCapacity;

			double suppliedPower = 0;
			for (int k = 0; k < r.producers.length; k++) {
				if (suppliedPower >= requiredLoad)
					break;

				Producer producer = r.producers[k];
				Boiler boiler = producer.getBoiler();
				if (maxLoad < boiler.getMinPower())
					continue;

				double power = getSuppliedPower(requiredLoad, maxLoad, producer);
				if (power > requiredLoad) {
					bufferCapacity -= (power - requiredLoad);
				}
				suppliedPower += power;
				maxLoad -= power;
				requiredLoad -= power;
				r.producerResults[k][i] = power;

				if (suppliedPower < requiredLoad
						&& bufferPotential >= requiredLoad) {
					// take rest from buffer
					break;
				}
			}

			if (suppliedPower < requiredLoad && bufferPotential > 0) {
				double bufferPower = requiredLoad;
				if (bufferPotential < requiredLoad)
					bufferPower = bufferPotential;
				suppliedPower += bufferPower;
				r.suppliedBufferHeat[i] = bufferPower;
				bufferCapacity += bufferPower;
			}
			r.bufferCapacity[i] = bufferCapacity;
			r.suppliedPower[i] = suppliedPower;
			r.printRow(i);
		}

		return r;
	}

	private double getSuppliedPower(double requiredLoad, double maxLoad,
			Producer producer) {
		double power;
		Boiler boiler = producer.getBoiler();
		if (producer.getFunction() == ProducerFunction.BASE_LOAD) {
			// TODO: producer running? -> buffer sufficient?
			power = maxLoad;
			if (boiler.getMaxPower() < maxLoad)
				power = boiler.getMaxPower();
		} else {
			power = requiredLoad;
			if (boiler.getMinPower() > requiredLoad)
				power = boiler.getMinPower();
		}
		return power;
	}

	private double getBufferCapacity(ProjectResult r, int i) {
		if (i == 0)
			return 0.5 * maxBufferCapacity;
		else
			return r.bufferCapacity[i - 1];
	}

	private double calcMaxBufferCapacity() {
		HeatNet net = project.getHeatNet();
		if (net == null)
			return 0;
		double liters = net.getBufferTankVolume();
		double deltaT = net.getSupplyTemperature() - net.getReturnTemperature();
		double c = 1.166; // Wh/(kg K)
		return (c * liters * deltaT) / 1000;
	}

}
