package sophena.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_product_entries")
public class ProductEntry extends AbstractEntity {

	@OneToOne
	@JoinColumn(name = "f_product")
	public Product product;

	@Embedded
	public ProductCosts costs;

	@Column(name = "price_per_piece")
	public double pricePerPiece;

	@Column(name = "number_of_items")
	public double count;

	@Override
	public ProductEntry clone() {
		ProductEntry clone = new ProductEntry();
		clone.id = UUID.randomUUID().toString();
		clone.product = product;
		if (costs != null)
			clone.costs = costs.clone();
		clone.pricePerPiece = pricePerPiece;
		clone.count = count;
		return clone;
	}

}
