package sophena.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
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
	public BufferTank bufferTank;

	@Column(name = "buffer_tank_volume")
	public double bufferTankVolume;

	@Embedded
	public ComponentCosts bufferTankCosts;

	@Column(name = "power_loss")
	public double powerLoss;

	@Column(name = "with_interruption")
	public boolean withInterruption;

	@Column(name = "interruption_start")
	public String interruptionStart;

	@Column(name = "interruption_end")
	public String interruptionEnd;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_project")
	public final List<HeatNetPipe> pipes = new ArrayList<>();

	@Override
	public HeatNet clone() {
		HeatNet clone = new HeatNet();
		clone.bufferTankVolume = bufferTankVolume;
		clone.bufferTank = bufferTank;
		if (bufferTankCosts != null)
			clone.bufferTankCosts = bufferTankCosts.clone();
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
