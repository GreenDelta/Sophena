package sophena.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_producers")
public class Producer extends Facility {

	@OneToOne
	@JoinColumn(name = "f_boiler")
	private Boiler boiler;

	public Boiler getBoiler() {
		return boiler;
	}

	public void setBoiler(Boiler boiler) {
		this.boiler = boiler;
	}
}
