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

	/// The total heat loss coefficient of the network in W/K.
	/// Sum of `pipe.length * pipe.uValue` over all pipes.
	public static double heatLossCoefficientOf(HeatNet net) {
		if (net == null)
			return 0;
		double sum = 0;
		for (HeatNetPipe p : net.pipes) {
			sum += heatLossCoefficientOf(p);
		}
		return sum;
	}

	/// The heat loss coefficient of a single pipe in W/K:
	/// `pipe.length * pipe.pipe.uValue`.
	public static double heatLossCoefficientOf(HeatNetPipe pipe) {
		return pipe != null && pipe.pipe != null
			? pipe.length * pipe.pipe.uValue
			: 0;
	}
}
