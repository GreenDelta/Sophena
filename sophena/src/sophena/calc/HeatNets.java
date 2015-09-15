package sophena.calc;

import sophena.model.HeatNet;
import sophena.model.HeatNetPipe;

public class HeatNets {

	private HeatNets() {
	}

	/**
	 * Calculates the length of the heating net from the pipes that are used.
	 */
	public static double calculateLength(HeatNet net) {
		if (net == null)
			return 0;
		double sum = 0;
		for (HeatNetPipe p : net.pipes)
			sum += p.length;
		return sum;
	}

	/**
	 * Calculates the specific power loss of a heating net from the pipes.
	 */
	public static double calculatePowerLoss(HeatNet net) {
		if (net == null)
			return 0;
		double totalLength = calculateLength(net);
		if (totalLength <= 0)
			return 0;
		double sum = 0;
		for (HeatNetPipe p : net.pipes) {
			sum += getPowerLoss(p, net) * p.length;
		}
		return sum / totalLength;
	}

	/**
	 * Get the power loss of the given pipe in the given heating net.
	 */
	public static double getPowerLoss(HeatNetPipe pipe, HeatNet net) {
		if (pipe == null || pipe.pipe == null || net == null)
			return 0;
		double tNet = (net.supplyTemperature + net.returnTemperature) / 2;
		double tempDelta = tNet - 10;
		return tempDelta * pipe.pipe.uValue;
	}

}
