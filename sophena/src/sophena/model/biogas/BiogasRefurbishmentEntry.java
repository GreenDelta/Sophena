package sophena.model.biogas;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import sophena.model.AbstractEntity;
import sophena.model.Product;
import sophena.model.ProductCosts;

/// An investment into an existing asset of a biogas plant that is refurbished
/// or overhauled. In contrast to a normal investment entry
/// (`sophena.model.ProductEntry`) only a share of the given investment is
/// spent for the refurbishment, see `refurbishmentShare`.
@Entity
@Table(name = "tbl_biogas_refurbishment_entries")
public class BiogasRefurbishmentEntry extends AbstractEntity {

	@OneToOne
	@JoinColumn(name = "f_product")
	public Product product;

	@Embedded
	public ProductCosts costs;

	@Column(name = "price_per_piece")
	public double pricePerPiece;

	@Column(name = "number_of_items")
	public double count;

	/// The share [%] of the given investment that is spent for the
	/// refurbishment or general overhaul of the asset.
	@Column(name = "refurbishment_share")
	public double refurbishmentShare;

	@Override
	public BiogasRefurbishmentEntry copy() {
		var clone = new BiogasRefurbishmentEntry();
		clone.id = UUID.randomUUID().toString();
		clone.product = product;
		if (costs != null) {
			clone.costs = costs.copy();
		}
		clone.pricePerPiece = pricePerPiece;
		clone.count = count;
		clone.refurbishmentShare = refurbishmentShare;
		return clone;
	}
}
