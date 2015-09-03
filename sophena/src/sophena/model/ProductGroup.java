package sophena.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_product_groups")
public class ProductGroup extends RootEntity {

	@Enumerated(EnumType.STRING)
	@Column(name = "product_type")
	public ProductType type;

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
		clone.duration = duration;
		clone.repair = repair;
		clone.maintenance = maintenance;
		clone.operation = operation;
		return clone;
	}
}
