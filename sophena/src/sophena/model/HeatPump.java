package sophena.model;

import java.util.Arrays;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
	@Convert(converter = DoubleArrayConverter.class)
	public double[] maxPower;

	@Column(name = "cop")
	@Convert(converter = DoubleArrayConverter.class)
	public double[] cop;

	@Column(name = "target_temperature")
	@Convert(converter = DoubleArrayConverter.class)
	public double[] targetTemperature;

	@Column(name = "source_temperature")
	@Convert(converter = DoubleArrayConverter.class)
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
