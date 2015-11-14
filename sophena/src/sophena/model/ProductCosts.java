package sophena.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ProductCosts {

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
	public ProductCosts clone() {
		ProductCosts clone = new ProductCosts();
		clone.investment = investment;
		clone.duration = duration;
		clone.repair = repair;
		clone.maintenance = maintenance;
		clone.operation = operation;
		return clone;
	}

	/**
	 * Copies the default values of the given product group to the given product
	 * costs.
	 */
	public static void copy(ProductGroup fromGroup, ProductCosts toCosts) {
		if (toCosts == null)
			return;
		if (fromGroup == null) {
			toCosts.duration = 0;
			toCosts.maintenance = 0;
			toCosts.operation = 0;
			toCosts.repair = 0;
		} else {
			toCosts.duration = fromGroup.duration;
			toCosts.maintenance = fromGroup.maintenance;
			toCosts.operation = fromGroup.operation;
			toCosts.repair = fromGroup.repair;
		}
	}
}
