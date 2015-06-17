package sophena.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ComponentCosts {

	@Column(name = "investment")
	public double investment;

	@Column(name = "duration")
	public int duration;

	@Column(name = "repair")
	public double repair;

	@Column(name = "maintenance")
	public double maintenance;

	@Column(name = "operation")
	public double operation;

	@Override
	public ComponentCosts clone() {
		ComponentCosts clone = new ComponentCosts();
		clone.duration = duration;
		clone.investment = investment;
		clone.maintenance = maintenance;
		clone.operation = operation;
		clone.repair = repair;
		return clone;
	}
}
