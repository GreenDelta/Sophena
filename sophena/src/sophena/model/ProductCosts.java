package sophena.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ProductCosts implements Copyable<ProductCosts> {

	/**
	 * The purchase price of the product in EUR.
	 */
	@Column(name = "investment")
	public double investment;

	/**
	 * The usage duration of the product in years.
	 */
	@Column(name = "duration")
	public int duration;

	/**
	 * Fraction [%] of the investment that is used for repair.
	 */
	@Column(name = "repair")
	public double repair;

	/**
	 * Fraction [%] of the investment that is used for maintenance .
	 */
	@Column(name = "maintenance")
	public double maintenance;

	/**
	 * Hours per year that are used for operation of the product.
	 */
	@Column(name = "operation")
	public double operation;

	@Override
	public ProductCosts copy() {
		var clone = new ProductCosts();
		clone.investment = investment;
		clone.duration = duration;
		clone.repair = repair;
		clone.maintenance = maintenance;
		clone.operation = operation;
		return clone;
	}

	public static void copy(AbstractProduct product, ProductCosts toCosts) {
		if (toCosts == null)
			return;
		if (product == null) {
			toCosts.investment = 0;
			toCosts.duration = 0;
			toCosts.maintenance = 0;
			toCosts.operation = 0;
			toCosts.repair = 0;
			return;
		}
		copy(product.group, toCosts);
		toCosts.investment = product.purchasePrice != null
				? product.purchasePrice
				: 0d;
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

	/**
	 * Returns true if the given costs are null or if there are no cost related
	 * entries in the given object.
	 */
	public static boolean isEmpty(ProductCosts costs) {
		if (costs == null)
			return true;
		return costs.investment == 0 && costs.operation == 0;
	}

	/**
	 * Sets all cost values to zero.
	 */
	public static void clear(ProductCosts costs) {
		if (costs == null)
			return;
		costs.investment = 0;
		costs.duration = 0;
		costs.repair = 0;
		costs.maintenance = 0;
		costs.operation = 0;
	}
}
