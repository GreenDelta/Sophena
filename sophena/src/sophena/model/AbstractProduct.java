package sophena.model;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;

@MappedSuperclass
public abstract class AbstractProduct extends BaseDataEntity {

	@Column(name = "purchase_price")
	public Double purchasePrice;

	@Column(name = "url")
	public String url;

	@Column(name = "product_line")
	public String productLine;

	@OneToOne
	@JoinColumn(name = "f_manufacturer")
	public Manufacturer manufacturer;

	@Enumerated(EnumType.STRING)
	@Column(name = "product_type")
	public ProductType type;

	@OneToOne
	@JoinColumn(name = "f_product_group")
	public ProductGroup group;

	static void copyFields(AbstractProduct from, AbstractProduct to) {
		if (from == null || to == null)
			return;
		to.name = from.name;
		to.description = from.description;
		to.isProtected = from.isProtected;
		to.purchasePrice = from.purchasePrice;
		to.url = from.url;
		to.productLine = from.productLine;
		to.manufacturer = from.manufacturer;
		to.type = from.type;
		to.group = from.group;
	}

}
