package sophena.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

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
	public double maxTemperature;

	@Column(name = "max_pressure")
	public double maxPressure;

}
