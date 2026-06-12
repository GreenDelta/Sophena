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

	/// Calculates the total heat loss of the network per Kelvin temperature
	/// difference in W/K.
	public static double heatLossCoefficientOf(HeatNet net) {
		if (net == null)
			return 0;
		double sum = 0;
		for (HeatNetPipe p : net.pipes) {
			sum += heatLossCoefficientOf(p);
		}
		return sum;
	}


	/// Calculates the heat loss per Kelvin of the given pipe in W/K.
	public static double heatLossCoefficientOf(HeatNetPipe pipe) {
		return pipe != null && pipe.pipe != null
			? pipe.length * pipe.pipe.uValue
			: 0;
	}
}
