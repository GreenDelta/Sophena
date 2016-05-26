package sophena.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_heat_nets")
public class HeatNet extends AbstractEntity {

	@Column(name = "net_length")
	public double length;

	@Column(name = "supply_temperature")
	public double supplyTemperature;

	@Column(name = "return_temperature")
	public double returnTemperature;

	@Column(name = "simultaneity_factor")
	public double simultaneityFactor;

	@Column(name = "smoothing_factor")
	public double smoothingFactor;

	@Column(name = "max_load")
	public Double maxLoad;

	@OneToOne
	@JoinColumn(name = "f_buffer_tank")
	public BufferTank bufferTank;

	@Column(name = "buffer_tank_volume")
	public double bufferTankVolume;

	@Column(name = "max_buffer_load_temperature")
	public double maxBufferLoadTemperature;

	@Embedded
	public ProductCosts bufferTankCosts;

	@Column(name = "power_loss")
	public double powerLoss;

	@Column(name = "with_interruption")
	public boolean withInterruption;

	@Column(name = "interruption_start")
	public String interruptionStart;

	@Column(name = "interruption_end")
	public String interruptionEnd;

	@JoinColumn(name = "f_heat_net")
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	public final List<HeatNetPipe> pipes = new ArrayList<>();

	@Override
	public HeatNet clone() {
		HeatNet clone = new HeatNet();
		clone.id = UUID.randomUUID().toString();
		clone.bufferTankVolume = bufferTankVolume;
		clone.maxBufferLoadTemperature = maxBufferLoadTemperature;
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
		for (HeatNetPipe p : pipes)
			clone.pipes.add(p.clone());
		return clone;
	}
}
