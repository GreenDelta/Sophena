package sophena.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ComponentCosts {

	/** The purchase price of the product in EUR. */
	@Column(name = "investment")
	public double investment;

	/** The usage duration of the product. */
	@Column(name = "duration")
	public int duration;

	/** Fraction [%] of the investment that is used for repair. */
	@Column(name = "repair")
	public double repair;

	/** Fraction [%] of the investment that is used for maintenance . */
	@Column(name = "maintenance")
	public double maintenance;

	/** Amount of hours that are used for operation in one year. */
	@Column(name = "operation")
	public double operation;

	@Override
	public ComponentCosts clone() {
		ComponentCosts clone = new ComponentCosts();
		clone.investment = investment;
		clone.duration = duration;
		clone.repair = repair;
		clone.maintenance = maintenance;
		clone.operation = operation;
		return clone;
	}
}
