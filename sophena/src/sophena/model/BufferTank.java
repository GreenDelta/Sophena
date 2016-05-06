package sophena.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_buffer_tanks")
public class BufferTank extends AbstractProduct {

	@Column(name = "volume")
	public double volume;

	@Column(name = "diameter")
	public double diameter;

	@Column(name = "height")
	public double height;

	@Column(name = "insulation_thickness")
	public double insulationThickness;

}
