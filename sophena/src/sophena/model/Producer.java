package sophena.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_producers")
public class Producer extends Facility {

	@OneToOne
	@JoinColumn(name = "f_boiler")
	private Boiler boiler;

	@Column(name = "rank")
	private int rank;

	@Enumerated(EnumType.STRING)
	@Column(name = "producer_function")
	private ProducerFunction function;

	public Boiler getBoiler() {
		return boiler;
	}

	public void setBoiler(Boiler boiler) {
		this.boiler = boiler;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public ProducerFunction getFunction() {
		return function;
	}

	public void setFunction(ProducerFunction function) {
		this.function = function;
	}

	@Override
	public Producer clone() {
		Producer clone = new Producer();
		clone.setBoiler(getBoiler());
		clone.setId(getId());
		clone.setName(getName());
		clone.setFunction(getFunction());
		clone.setRank(getRank());
		clone.setDescription(getDescription());
		return clone;
	}

}
