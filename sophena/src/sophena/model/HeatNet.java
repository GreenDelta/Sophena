package sophena.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

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
	public Double smoothingFactor;

	@Column(name = "max_load")
	public Double maxLoad;

	@OneToOne
	@JoinColumn(name = "f_buffer_tank")
	public BufferTank bufferTank;

	@Column(name = "max_buffer_load_temperature")
	public double maxBufferLoadTemperature;

	@Column(name = "lower_buffer_load_temperature")
	public Double lowerBufferLoadTemperature;

	@Column(name = "buffer_lambda")
	public double bufferLambda;

	@Embedded
	public ProductCosts bufferTankCosts;

	/** Loss of the heating network per meter of net length in [W/m]. */
	@Column(name = "power_loss")
	public double powerLoss;

	@JoinColumn(name = "f_interruption")
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	public TimeInterval interruption;

	@JoinColumn(name = "f_heat_net")
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	public final List<HeatNetPipe> pipes = new ArrayList<>();

	@Override
	public HeatNet clone() {
		HeatNet clone = new HeatNet();
		clone.id = UUID.randomUUID().toString();
		clone.maxBufferLoadTemperature = maxBufferLoadTemperature;
		clone.lowerBufferLoadTemperature = lowerBufferLoadTemperature;
		clone.bufferLambda = bufferLambda;
		clone.bufferTank = bufferTank;
		if (bufferTankCosts != null)
			clone.bufferTankCosts = bufferTankCosts.clone();
		clone.length = length;
		clone.powerLoss = powerLoss;
		clone.returnTemperature = returnTemperature;
		clone.simultaneityFactor = simultaneityFactor;
		clone.smoothingFactor = smoothingFactor;
		clone.maxLoad = maxLoad;
		clone.supplyTemperature = supplyTemperature;
		if (interruption != null) {
			clone.interruption = interruption.clone();
		}
		for (HeatNetPipe p : pipes)
			clone.pipes.add(p.clone());
		return clone;
	}
}
