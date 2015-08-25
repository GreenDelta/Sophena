package sophena.model;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_buffer_tanks")
public class BufferTank extends Product {

	public double volume;

}
