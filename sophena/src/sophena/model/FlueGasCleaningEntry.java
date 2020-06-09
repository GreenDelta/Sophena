package sophena.model;

import java.util.UUID;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_flue_gas_cleaning_entries")
public class FlueGasCleaningEntry extends AbstractEntity {

	@OneToOne
	@JoinColumn(name = "f_flue_gas_cleaning")
	public FlueGasCleaning product;

	@Embedded
	public ProductCosts costs;

	@Override
	public FlueGasCleaningEntry clone() {
		FlueGasCleaningEntry clone = new FlueGasCleaningEntry();
		clone.id = UUID.randomUUID().toString();
		clone.product = product;
		if (costs != null) {
			clone.costs = costs.clone();
		}
		return clone;
	}
}