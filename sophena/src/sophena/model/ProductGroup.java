package sophena.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_product_groups")
public class ProductGroup extends BaseDataEntity {

	@Enumerated(EnumType.STRING)
	@Column(name = "product_type")
	public ProductType type;

	/**
	 * Product groups that contain heat producers must have a fuel group
	 * assigned.
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "fuel_group")
	public FuelGroup fuelGroup;

	/** This is just for ordering the groups in the user interface. */
	@Column(name = "idx")
	public int index;

	/** Default usage duration of this product group given in years. */
	@Column(name = "duration")
	public int duration;

	/** Default fraction [%] of the investment that is used for repair. */
	@Column(name = "repair")
	public double repair;

	/** Default fraction [%] of the investment that is used for maintenance . */
	@Column(name = "maintenance")
	public double maintenance;

	/** Default amount of hours that are used for operation in one year. */
	@Column(name = "operation")
	public double operation;

	@Override
	public ProductGroup clone() {
		ProductGroup clone = new ProductGroup();
		clone.id = UUID.randomUUID().toString();
		clone.name = name;
		clone.description = description;
		clone.type = type;
		clone.fuelGroup = fuelGroup;
		clone.duration = duration;
		clone.repair = repair;
		clone.maintenance = maintenance;
		clone.operation = operation;
		return clone;
	}
}
