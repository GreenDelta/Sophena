package sophena.math.energetic;

import sophena.model.HeatNet;

public class Buffers {

	private Buffers() {
	}

	/**
	 * Calculates the buffer tank capacity of the given heating net
	 * specification, in kWh.
	 */
	public static double maxCapacity(HeatNet net) {
		if (net == null)
			return 0;
		double volume = net.bufferTankVolume; // liters
		double maxTemp = net.maxBufferLoadTemperature;
		double minTemp = net.lowerBufferLoadTemperature != null
				? net.lowerBufferLoadTemperature
				: net.supplyTemperature;
		return 0.001166 * volume * (maxTemp - minTemp);
	}

}
