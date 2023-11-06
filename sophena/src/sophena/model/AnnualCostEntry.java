package sophena.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * A data structure to store other annual costs in the project cost settings.
 */
@Embeddable
public class AnnualCostEntry implements Copyable<AnnualCostEntry> {

	@Column(name = "label")
	public String label;

	@Column(name = "cost_entry")
	public double value;

	@Override
	public AnnualCostEntry copy() {
		var clone = new AnnualCostEntry();
		clone.label = label;
		clone.value = value;
		return clone;
	}
}
