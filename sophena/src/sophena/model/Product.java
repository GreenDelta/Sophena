package sophena.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_products")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class Product extends RootEntity {

	@Column(name = "purchase_price")
	public Double purchasePrice;

	@Column(name = "url")
	public String url;

	@Enumerated(EnumType.STRING)
	@Column(name = "product_type")
	public ProductType type;

	@OneToOne
	@JoinColumn(name = "f_product_group")
	public ProductGroup group;

	@Override
	public Product clone() {
		Product clone = new Product();
		clone.id = UUID.randomUUID().toString();
		clone.name = name;
		clone.description = description;
		clone.purchasePrice = purchasePrice;
		clone.url = url;
		clone.type = type;
		clone.group = group;
		return clone;
	}
}
