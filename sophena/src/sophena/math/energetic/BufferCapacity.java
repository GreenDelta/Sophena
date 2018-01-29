package sophena.math.energetic;

import sophena.model.HeatNet;

public class BufferCapacity {

	/** The buffer tank volume in litre. */
	public double volume;

	/** The maximum charging temperature of the buffer tank, in °C */
	public double maxChargingTemperatur;

	/** The lower charging temperature of the buffer tank, in °C */
	public double lowerChargingTemperatur;

	/**
	 * Calulcates the value of the buffer capacity, in kWh
	 */
	public double value() {
		return 0.001166 * volume * (maxChargingTemperatur - lowerChargingTemperatur);
	}

	/**
	 * Calulcates the buffer tank capacity of the given heating net
	 * specification, in kWh.
	 */
	public static double of(HeatNet net) {
		if (net == null)
			return 0;
		BufferCapacity cap = new BufferCapacity();
		cap.volume = net.bufferTankVolume;
		cap.maxChargingTemperatur = net.maxBufferLoadTemperature;
		cap.lowerChargingTemperatur = net.returnTemperature;
		if (net.lowerBufferLoadTemperature != null) {
			cap.lowerChargingTemperatur = net.lowerBufferLoadTemperature;
		}
		return cap.value();
	}

}
