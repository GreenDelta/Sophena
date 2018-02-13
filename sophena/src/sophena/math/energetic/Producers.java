package sophena.math.energetic;

import sophena.model.HeatRecovery;
import sophena.model.Producer;

public class Producers {

	private Producers() {
	}

	public static double minPower(Producer p) {
		if (p == null || p.boiler == null)
			return 0;
		return p.boiler.minPower * heatRecoveryFactor(p);
	}

	public static double maxPower(Producer p) {
		if (p == null || p.boiler == null)
			return 0;
		return p.boiler.maxPower * heatRecoveryFactor(p);
	}

	public static double efficiencyRate(Producer p) {
		if (p == null)
			return 0;
		return p.boiler.efficiencyRate * heatRecoveryFactor(p);
	}

	private static double heatRecoveryFactor(Producer p) {
		if (p == null || p.heatRecovery == null)
			return 1;
		HeatRecovery hr = p.heatRecovery;
		return 1 + (hr.power / hr.producerPower);
	}
}
