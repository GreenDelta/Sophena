package sophena.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_pipes")
public class Pipe extends AbstractProduct {

	@Column(name = "u_value")
	public double uValue;

	@Column(name = "diameter ")
	public double diameter;

}
