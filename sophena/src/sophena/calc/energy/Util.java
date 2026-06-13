package sophena.calc.energy;

import sophena.math.energetic.Producers;
import sophena.model.Producer;
import sophena.model.Stats;

class Util {

	/**
	 * Get the minimum power of the given producer for the given hour (used in
	 * energy simulations).
	 */
	static double minPower(Producer p, int hour) {
		if (p == null)
			return 0;
		if (p.hasProfile()) {
			if (p.profile == null)
				return 0;
			return Stats.get(p.profile.minPower, hour);
		}
		if (p.boiler == null)
			return 0;
		return p.boiler.minPower * Producers.heatRecoveryFactor(p);
	}


	/**
	 * Get the minimum power of the given producer for the given hour (used in
	 * energy simulations).
	 */
	static double maxPower(Producer p, SolarState solarState, HeatPumpState heatPumpState, int hour) {
		if (p == null)
			return 0;
		if (p.hasProfile()) {
			if (p.profile == null)
				return 0;
			return Stats.get(p.profile.maxPower, hour);
		}
		if(p.solarCollector !=null && solarState != null)
			return solarState.getAvailablePowerInKWh();
		if(p.heatPump != null && heatPumpState != null)
			return heatPumpState.getMaxPower();
		if (p.boiler == null)
			return 0;
		return p.boiler.maxPower * Producers.heatRecoveryFactor(p);
	}

}
