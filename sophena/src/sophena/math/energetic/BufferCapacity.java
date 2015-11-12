package sophena.math.energetic;

import sophena.model.HeatNet;

public class BufferCapacity {

	private final double volume;
	private double maxChargingTemperatur;
	private double returnTemperature;

	private BufferCapacity(double volume) {
		this.volume = volume;
	}

	public static BufferCapacity ofVolume_L(double volume) {
		return new BufferCapacity(volume);
	}

	public BufferCapacity maxChargingTemperatur_degC(double maxTemperatur) {
		this.maxChargingTemperatur = maxTemperatur;
		return this;
	}

	public BufferCapacity returnTemperature_degC(double returnTemperature) {
		this.returnTemperature = returnTemperature;
		return this;
	}

	public double get_kWh() {
		return 0.001166 * volume * (maxChargingTemperatur - returnTemperature);
	}

	public static double get(HeatNet net) {
		if (net == null)
			return 0;
		double maxTemp = net.bufferTank == null ? net.supplyTemperature
				: net.bufferTank.maxChargingTemperature;
		return BufferCapacity
				.ofVolume_L(net.bufferTankVolume)
				.maxChargingTemperatur_degC(maxTemp)
				.returnTemperature_degC(net.returnTemperature)
				.get_kWh();
	}

}
