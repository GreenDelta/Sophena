package sophena.model.biogas;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
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

	@OneToOne
	@JoinColumn(name = "f_electricity_price_curve")
	public ElectricityPriceCurve electricityPrices;

	/// rated power in kW
	@Column(name = "rated_power")
	public double ratedPower;

	/// minimum runtime in hours
	@Column(name = "minimum_runtime")
	public int minimumRuntime;

	/// product costs for this biogas plant
	@Embedded
	public ProductCosts costs;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_biogas_plant")
	public final List<SubstrateProfile> substrateProfiles = new ArrayList<>();


	@Override
	public BiogasPlant copy() {
		var clone = new BiogasPlant();
		clone.id = UUID.randomUUID().toString();
		clone.name = name;
		clone.description = description;
		clone.producedElectricity = producedElectricity;
		clone.product = product;
		clone.productGroup = productGroup;
		clone.electricityPrices = electricityPrices;
		clone.ratedPower = ratedPower;
		clone.minimumRuntime = minimumRuntime;
		clone.costs = costs != null ? costs.copy() : null;
		for (var p : substrateProfiles) {
			clone.substrateProfiles.add(p.copy());
		}
		return clone;
	}
}
