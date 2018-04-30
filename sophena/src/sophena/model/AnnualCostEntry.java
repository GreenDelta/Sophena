package sophena.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * A data structure to store other annual costs in the project cost settings.
 */
@Embeddable
public class AnnualCostEntry {

	@Column(name = "label")
	public String label;

	@Column(name = "cost_entry")
	public double value;

	@Override
	public AnnualCostEntry clone() {
		AnnualCostEntry clone = new AnnualCostEntry();
		clone.label = label;
		clone.value = value;
		return clone;
	}
}
