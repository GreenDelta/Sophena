package sophena.math.energetic;

import sophena.model.CostSettings;

public class UsedElectricity {

	/**
	 * Returns the amount of used electricity [kWh] to produce the given amount
	 * of heat [kWh] with the given settings.
	 */
	public static double get(double producedHeat, CostSettings settings) {
		if (settings == null)
			return 0;
		return producedHeat * settings.electricityDemandShare / 100;
	}

}
