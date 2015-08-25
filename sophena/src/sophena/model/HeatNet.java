package sophena.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Embeddable
public class HeatNet {

	@Column(name = "net_length")
	public double length;

	@Column(name = "supply_temperature")
	public double supplyTemperature;

	@Column(name = "return_temperature")
	public double returnTemperature;

	@Column(name = "simultaneity_factor")
	public double simultaneityFactor;

	@Column(name = "max_load")
	public double maxLoad;

	@OneToOne
	@JoinColumn(name = "f_buffer_tank")
	public BufferTank BufferTank;

	@Column(name = "buffer_tank_volume")
	public double bufferTankVolume;

	@Column(name = "power_loss")
	public double powerLoss;

	@Column(name = "with_interruption")
	public boolean withInterruption;

	@Column(name = "interruption_start")
	public String interruptionStart;

	@Column(name = "interruption_end")
	public String interruptionEnd;

	@Override
	public HeatNet clone() {
		HeatNet clone = new HeatNet();
		clone.bufferTankVolume = bufferTankVolume;
		clone.length = length;
		clone.powerLoss = powerLoss;
		clone.returnTemperature = returnTemperature;
		clone.simultaneityFactor = simultaneityFactor;
		clone.maxLoad = maxLoad;
		clone.supplyTemperature = supplyTemperature;
		clone.withInterruption = withInterruption;
		clone.interruptionStart = interruptionStart;
		clone.interruptionEnd = interruptionEnd;
		return clone;
	}
}
