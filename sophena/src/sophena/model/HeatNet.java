package sophena.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class HeatNet {

	@Column(name = "net_length")
	private double length;

	@Column(name = "supply_temperature")
	private double supplyTemperature;

	@Column(name = "return_temperature")
	private double returnTemperature;

	@Column(name = "simultaneity_factor")
	private double simultaneityFactor;

	@Column(name = "buffer_tank_volume")
	private double bufferTankVolume;

	@Column(name = "power_loss")
	private double powerLoss;

	@Column(name = "with_interruption")
	private boolean withInterruption;

	@Column(name = "interruption_start")
	private String interruptionStart;

	@Column(name = "interruption_end")
	private String interruptionEnd;

	public double getLength() {
		return length;
	}

	public void setLength(double length) {
		this.length = length;
	}

	public double getSupplyTemperature() {
		return supplyTemperature;
	}

	public void setSupplyTemperature(double supplyTemperature) {
		this.supplyTemperature = supplyTemperature;
	}

	public double getReturnTemperature() {
		return returnTemperature;
	}

	public void setReturnTemperature(double returnTemperature) {
		this.returnTemperature = returnTemperature;
	}

	public double getSimultaneityFactor() {
		return simultaneityFactor;
	}

	public void setSimultaneityFactor(double simultaneityFactor) {
		this.simultaneityFactor = simultaneityFactor;
	}

	public double getBufferTankVolume() {
		return bufferTankVolume;
	}

	public void setBufferTankVolume(double bufferTankVolume) {
		this.bufferTankVolume = bufferTankVolume;
	}

	public double getPowerLoss() {
		return powerLoss;
	}

	public void setPowerLoss(double powerLoss) {
		this.powerLoss = powerLoss;
	}

	public boolean isWithInterruption() {
		return withInterruption;
	}

	public void setWithInterruption(boolean withInterruption) {
		this.withInterruption = withInterruption;
	}

	public String getInterruptionStart() {
		return interruptionStart;
	}

	public void setInterruptionStart(String interruptionStart) {
		this.interruptionStart = interruptionStart;
	}

	public String getInterruptionEnd() {
		return interruptionEnd;
	}

	public void setInterruptionEnd(String interruptionEnd) {
		this.interruptionEnd = interruptionEnd;
	}

	@Override
	public HeatNet clone() {
		HeatNet clone = new HeatNet();
		clone.setBufferTankVolume(getBufferTankVolume());
		clone.setLength(getLength());
		clone.setPowerLoss(getPowerLoss());
		clone.setReturnTemperature(getReturnTemperature());
		clone.setSimultaneityFactor(getSimultaneityFactor());
		clone.setSupplyTemperature(getSupplyTemperature());
		clone.setWithInterruption(isWithInterruption());
		clone.setInterruptionStart(getInterruptionStart());
		clone.setInterruptionEnd(getInterruptionEnd());
		return clone;
	}
}
