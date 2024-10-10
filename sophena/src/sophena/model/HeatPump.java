package sophena.model;

import java.util.Arrays;
import java.util.UUID;

import org.eclipse.persistence.annotations.Convert;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_heat_pumps")
public class HeatPump extends AbstractProduct {

	@Column(name = "min_power")
	public double minPower;
	
	@Column(name = "rated_power")
	public double ratedPower;

	@Column(name = "max_power")
	@Convert("DoubleArrayConverter")
	public double[] maxPower;
	
	@Column(name = "cop")
	@Convert("DoubleArrayConverter")
	public double[] cop;

	@Column(name = "target_temperature")
	@Convert("DoubleArrayConverter")
	public double[] targetTemperature;
	
	@Column(name = "source_temperature")
	@Convert("DoubleArrayConverter")
	public double[] sourceTemperature;

	@Override
	public HeatPump copy() {
		var clone = new HeatPump();
		AbstractProduct.copyFields(this, clone);
		clone.id = UUID.randomUUID().toString();
		clone.minPower = minPower;
		clone.ratedPower = ratedPower;
		clone.maxPower = maxPower != null
				? Arrays.copyOf(maxPower, maxPower.length)
				: null;
		clone.cop = cop != null
				? Arrays.copyOf(cop, cop.length)
				: null;
		clone.targetTemperature = targetTemperature != null
				? Arrays.copyOf(targetTemperature, targetTemperature.length)
				: null;
		clone.sourceTemperature = sourceTemperature != null
				? Arrays.copyOf(sourceTemperature, sourceTemperature.length)
				: null;
		return clone;
	}
}
