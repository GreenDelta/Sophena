package sophena.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_buffer_tanks")
public class BufferTank extends Product {

	@Column(name = "volume")
	public double volume;

}
