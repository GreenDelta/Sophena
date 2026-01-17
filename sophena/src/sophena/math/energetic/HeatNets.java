package sophena.math.energetic;

import sophena.model.HeatNet;
import sophena.model.HeatNetPipe;
import sophena.model.PipeType;

public class HeatNets {

	private HeatNets() {
	}

	/// Calculates the trench length of the heating net from the pipe lengths.	
	public static double getTrenchLengthOf(HeatNet net) {
		if (net == null)
			return 0;
		double sum = 0;
		for (var p : net.pipes) {
			sum += trenchLengthOf(p);
		}
		return sum;
	}
	
	private static double trenchLengthOf(HeatNetPipe pipe) {
		if (pipe == null)
			return 0;
		return pipe.pipe != null && pipe.pipe.pipeType == PipeType.UNO
			? pipe.length / 2
			: pipe.length;
	}

	/**
	 * Calculates the specific power loss of a heating net from the pipes.
	 */
	public static double calculatePowerLoss(HeatNet net) {
		if (net == null)
			return 0;
		double supplyLength = getTrenchLengthOf(net);
		if (supplyLength <= 0)
			return 0;
		double sum = 0;
		for (HeatNetPipe p : net.pipes) {
			sum += getPowerLoss(p, net);
		}
		return sum;
	}

	/**
	 * Get the power loss per K of the given pipe in the given heating net.
	 */
	public static double getPowerLoss(HeatNetPipe pipe, HeatNet net) {
		if (pipe == null || pipe.pipe == null || net == null)
			return 0;
		return pipe.length * pipe.pipe.uValue;
	}

}
