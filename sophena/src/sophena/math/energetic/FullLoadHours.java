package sophena.math.energetic;

import sophena.model.Producer;

public class FullLoadHours {

	/**
	 * Returns the full load hours [h] for the given generated heat [kWh] and
	 * nominal output of a boiler [kW].
	 */
	public static double get(double generatedHeat, double boilerPower) {
		if (boilerPower <= 0)
			return 0;
		return generatedHeat / boilerPower;
	}

	public static double get(Producer producer, double generatedHeat) {
		if (producer == null || producer.boiler == null)
			return 0;
		return get(generatedHeat, Producers.maxPower(producer));
	}
}
