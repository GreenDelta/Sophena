package sophena.model;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_buffers")
public class Buffer extends Product {

	public double volume;

}
