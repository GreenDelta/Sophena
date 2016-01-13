package sophena.model;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;

@MappedSuperclass
public abstract class AbstractProduct extends RootEntity {

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
}
