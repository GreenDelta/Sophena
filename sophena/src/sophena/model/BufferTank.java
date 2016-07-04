package sophena.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_buffer_tanks")
public class BufferTank extends AbstractProduct {

	@Column(name = "volume")
	public double volume;

	@Column(name = "diameter")
	public Double diameter;

	@Column(name = "height")
	public Double height;

	@Column(name = "insulation_thickness")
	public Double insulationThickness;

	@Override
	public BufferTank clone() {
		BufferTank clone = new BufferTank();
		AbstractProduct.copyFields(this, clone);
		clone.id = UUID.randomUUID().toString();
		clone.volume = volume;
		clone.diameter = diameter;
		clone.height = height;
		clone.insulationThickness = insulationThickness;
		return clone;
	}

}
