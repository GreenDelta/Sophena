package sophena.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class Product extends RootEntity {

	@Column(name = "purchase_price")
	public Double purchasePrice;

	@Column(name = "url")
	public String url;

}
