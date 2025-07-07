package sophena.model.biogas;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import sophena.model.Boiler;
import sophena.model.Fuel;
import sophena.model.ProductCosts;
import sophena.model.ProductGroup;
import sophena.model.RootEntity;

@Entity
@Table(name = "tbl_biogas_plants")
public class BiogasPlant extends RootEntity {

	@OneToOne
	@JoinColumn(name = "f_produced_electricity")
	public Fuel producedElectricity;

	@OneToOne
	@JoinColumn(name = "f_product")
	public Boiler product;

	@OneToOne
	@JoinColumn(name = "f_product_group")
	public ProductGroup productGroup;

	/// rated power in kW
	@Column(name = "rated_power")
	public double ratedPower;

	/// minimum runtime in hours
	@Column(name = "minimum_runtime")
	public int minimumRuntime;

	/// product costs for this biogas plant
	@Embedded
	public ProductCosts costs;

	@Override
	public BiogasPlant copy() {
		var clone = new BiogasPlant();
		clone.id = UUID.randomUUID().toString();
		clone.name = name;
		clone.description = description;
		clone.producedElectricity = producedElectricity;
		clone.product = product;
		clone.productGroup = productGroup;
		clone.ratedPower = ratedPower;
		clone.minimumRuntime = minimumRuntime;
		clone.costs = costs != null ? costs.copy() : null;
		return clone;
	}
}
