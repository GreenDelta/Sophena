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
	public boolean disabled;

	@OneToOne
	@JoinColumn(name = "f_boiler")
	public Boiler boiler;

	@Column(name = "rank")
	public int rank;

	@Enumerated(EnumType.STRING)
	@Column(name = "producer_function")
	public ProducerFunction function;

	@Embedded
	public ComponentCosts costs;

	@Embedded
	public FuelSpec fuelSpec;

	@Override
	public Producer clone() {
		Producer clone = new Producer();
		clone.id = UUID.randomUUID().toString();
		clone.name = name;
		clone.description = description;
		clone.disabled = disabled;
		clone.boiler = boiler;
		clone.function = function;
		clone.rank = rank;
		if (costs != null)
			clone.costs = costs.clone();
		if (fuelSpec != null)
			clone.fuelSpec = fuelSpec.clone();
		return clone;
	}

	public ProducerDescriptor toDescriptor() {
		ProducerDescriptor d = new ProducerDescriptor();
		d.id = id;
		d.name = name;
		d.description = description;
		d.setDisabled(disabled);
		return d;
	}

}
