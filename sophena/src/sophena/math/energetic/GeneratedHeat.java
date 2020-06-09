package sophena.math.energetic;

import sophena.calc.EnergyResult;

public class GeneratedHeat {

	private GeneratedHeat() {
	}

	/**
	 * Get the share of the given generated heat to the total load and buffer
	 * loss of the given energy result. The returned value is an integer between
	 * 0 and 100.
	 */
	public static int share(double heat, EnergyResult r) {
		if (heat == 0 || r == null)
			return 0;
		double total = r.totalLoad + r.totalBufferLoss;
		if (total <= 0)
			return 0;
		int share = (int) Math.round(100 * heat / total);
		return share > 100 ? 100 : share;
	}

}
