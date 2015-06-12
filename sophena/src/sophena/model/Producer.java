package sophena.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import sophena.model.descriptors.ProducerDescriptor;

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

	@Embedded
	private ComponentCosts costs = new ComponentCosts();

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

	public ComponentCosts getCosts() {
		return costs;
	}

	public void setCosts(ComponentCosts costs) {
		this.costs = costs;
	}

	@Override
	public Producer clone() {
		Producer clone = new Producer();
		clone.setId(UUID.randomUUID().toString());
		clone.setBoiler(getBoiler());
		clone.setName(getName());
		clone.setFunction(getFunction());
		clone.setRank(getRank());
		clone.setDescription(getDescription());
		if(getCosts() != null)
			clone.setCosts(getCosts().clone());
		return clone;
	}

	public ProducerDescriptor toDescriptor() {
		ProducerDescriptor d = new ProducerDescriptor();
		d.setId(getId());
		d.setName(getName());
		d.setDescription(getDescription());
		return d;
	}

}
