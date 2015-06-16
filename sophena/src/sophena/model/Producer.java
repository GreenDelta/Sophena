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

	@Column(name = "is_disabled")
	private boolean disabled;

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

	@Embedded
	private FuelSpec fuelSpec = new FuelSpec();

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

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

	public FuelSpec getFuelSpec() {
		return fuelSpec;
	}

	public void setFuelSpec(FuelSpec fuelSpec) {
		this.fuelSpec = fuelSpec;
	}

	@Override
	public Producer clone() {
		Producer clone = new Producer();
		clone.setId(UUID.randomUUID().toString());
		clone.setName(getName());
		clone.setDescription(getDescription());
		clone.setDisabled(isDisabled());
		clone.setBoiler(getBoiler());
		clone.setFunction(getFunction());
		clone.setRank(getRank());
		if (getCosts() != null)
			clone.setCosts(getCosts().clone());
		if (getFuelSpec() != null)
			clone.setFuelSpec(getFuelSpec().clone());
		return clone;
	}

	public ProducerDescriptor toDescriptor() {
		ProducerDescriptor d = new ProducerDescriptor();
		d.setId(getId());
		d.setName(getName());
		d.setDescription(getDescription());
		d.setDisabled(isDisabled());
		return d;
	}

}
