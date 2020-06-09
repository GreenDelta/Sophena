package sophena.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_buffer_tanks")
public class BufferTank extends AbstractProduct {

	/** The buffer tank volume in liters. */
	@Column(name = "volume")
	public double volume;

	/** The diameter of the buffer tank in mm. */
	@Column(name = "diameter")
	public double diameter;

	/** The height of the buffer tank in mm. */
	@Column(name = "height")
	public double height;

	/** The insulation thickness of the buffer tank in mm. */
	@Column(name = "insulation_thickness")
	public double insulationThickness;

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
