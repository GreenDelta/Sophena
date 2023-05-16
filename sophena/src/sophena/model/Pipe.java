package sophena.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_pipes")
public class Pipe extends AbstractProduct {

	@Column(name = "material")
	public String material;

	@Column(name = "pipe_type")
	public PipeType pipeType;

	@Column(name = "u_value")
	public double uValue;

	@Column(name = "inner_diameter")
	public double innerDiameter;

	@Column(name = "outer_diameter")
	public double outerDiameter;

	@Column(name = "total_diameter")
	public double totalDiameter;

	@Column(name = "delivery_type")
	public String deliveryType;

	@Column(name = "max_temperature")
	public Double maxTemperature;

	@Column(name = "max_pressure")
	public Double maxPressure;

	@Override
	public Pipe clone() {
		Pipe clone = new Pipe();
		AbstractProduct.copyFields(this, clone);
		clone.id = UUID.randomUUID().toString();
		clone.material = material;
		clone.pipeType = pipeType;
		clone.uValue = uValue;
		clone.innerDiameter = innerDiameter;
		clone.outerDiameter = outerDiameter;
		clone.totalDiameter = totalDiameter;
		clone.deliveryType = deliveryType;
		clone.maxTemperature = maxTemperature;
		clone.maxPressure = maxPressure;
		return clone;
	}
}
