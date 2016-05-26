package sophena.calc;

import sophena.model.HeatNet;
import sophena.model.HeatNetPipe;
import sophena.model.PipeType;

public class HeatNets {

	private HeatNets() {
	}

	public static double getSupplyLength(HeatNetPipe pipe) {
		if (pipe == null)
			return 0;
		if (pipe.pipe != null && pipe.pipe.pipeType == PipeType.UNO)
			return pipe.length / 2;
		else
			return pipe.length;
	}

	/**
	 * Calculates the length of the heating net from the pipes that are used.
	 */
	public static double getTotalSupplyLength(HeatNet net) {
		if (net == null)
			return 0;
		double sum = 0;
		for (HeatNetPipe p : net.pipes) {
			sum += getSupplyLength(p);
		}
		return sum;
	}

	/**
	 * Calculates the specific power loss of a heating net from the pipes.
	 */
	public static double calculatePowerLoss(HeatNet net) {
		if (net == null)
			return 0;
		double totalLength = getTotalSupplyLength(net);
		if (totalLength <= 0)
			return 0;
		double sum = 0;
		for (HeatNetPipe p : net.pipes) {
			double length = getSupplyLength(p);
			sum += getPowerLoss(p, net) * length;
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
