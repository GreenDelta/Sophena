package sophena.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

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
	public ProductEntry copy() {
		var clone = new ProductEntry();
		clone.id = UUID.randomUUID().toString();
		clone.product = product;
		if (costs != null) {
			clone.costs = costs.copy();
		}
		clone.pricePerPiece = pricePerPiece;
		clone.count = count;
		return clone;
	}

}
