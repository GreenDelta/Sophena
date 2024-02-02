package sophena.math.energetic;

import sophena.model.BufferTank;
import sophena.model.HeatNet;

public class Buffers {

	private Buffers() {
	}

	/**
	 * Calculates the buffer tank capacity of the given heating net
	 * specification, in kWh.
	 */
	public static double maxCapacity(HeatNet net) {
		if (net == null || net.bufferTank == null)
			return 0;
		double volume = net.bufferTank.volume; // liters
		double maxTemp = net.maxBufferLoadTemperature;
		double minTemp = net.lowerBufferLoadTemperature != null
				? net.lowerBufferLoadTemperature
				: net.returnTemperature;
		return 0.001166 * volume * (maxTemp - minTemp);
	}

	public static double capacity100(HeatNet net) {
		if (net == null || net.bufferTank == null)
			return 0;
		double volume = net.bufferTank.volume; // liters
		double maxTemp = net.supplyTemperature;
		double minTemp = net.lowerBufferLoadTemperature != null
				? net.lowerBufferLoadTemperature
				: net.returnTemperature;
		return 0.001166 * volume * (maxTemp - minTemp);
	}

	/**
	 * Calculates the static loss factor of the buffer tank in the given
	 * heatnet. This value is then combined with the current buffer state in an
	 * simulation step to calculate the buffer loss (see the loss function).
	 */
	public static double lossFactor(HeatNet net) {
		if (net == null || net.bufferTank == null)
			return 0;
		BufferTank buffer = net.bufferTank;
		double ins = buffer.insulationThickness / 1000;
		if (ins < 0.001) {
			ins = 0.001;
		}
		double r = (buffer.diameter / 1000 - (2 * ins)) / 2;
		double h = buffer.height / 1000 - (2 * ins);
		double area = 2 * Math.PI * r * (r + h);
		double uValue = net.bufferLambda / ins;
		return area * uValue;
	}

	/**
	 * Returns the buffer loss for the given fill rate (a value between 0 and
	 * 1).
	 */
	public static double loss(HeatNet net, double lossFactor, double fillRate) {
		if (net == null || lossFactor == 0)
			return 0;
		double maxTemp = net.maxBufferLoadTemperature;
		double minTemp = net.lowerBufferLoadTemperature != null
				? net.lowerBufferLoadTemperature
				: net.supplyTemperature;
		double deltaTemp = (minTemp + fillRate * (maxTemp - minTemp)) - 20;
		return lossFactor * deltaTemp / 1000;
	}

}
