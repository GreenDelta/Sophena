package sophena.model.biogas;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import sophena.model.AbstractEntity;
import sophena.model.ProductCosts;
import sophena.model.ProductGroup;

/// An investment into a biogas plant. Only the share that is defined in
/// `refurbishmentShare` is spent for the refurbishment or general overhaul of
/// an existing asset; a `null` value means that this is a normal investment.
@Entity
@Table(name = "tbl_biogas_investment_entries")
public class BiogasInvestmentEntry extends AbstractEntity {

	@OneToOne
	@JoinColumn(name = "f_product_group")
	public ProductGroup productGroup;

	@Enumerated(EnumType.STRING)
	@Column(name = "investment_group")
	public BiogasInvestmentGroup investmentGroup;

	/// A product name the users can enter.
	@Column(name = "name")
	public String name;

	@Embedded
	public ProductCosts costs = new ProductCosts();

	/// The share [%] of the given investment that is spent for the refurbishment
	/// or general overhaul of the asset. A value of `null` means that this is a
	/// normal investment.
	@Column(name = "refurbishment_share")
	public Double refurbishmentShare;

	@Override
	public BiogasInvestmentEntry copy() {
		var clone = new BiogasInvestmentEntry();
		clone.id = UUID.randomUUID().toString();
		clone.productGroup = productGroup;
		clone.investmentGroup = investmentGroup;
		clone.name = name;
		clone.costs = costs != null ? costs.copy() : new ProductCosts();
		clone.refurbishmentShare = refurbishmentShare;
		return clone;
	}
}
