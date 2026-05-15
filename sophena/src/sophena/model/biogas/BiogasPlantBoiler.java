package sophena.model.biogas;

import java.util.UUID;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import sophena.model.AbstractEntity;
import sophena.model.Boiler;
import sophena.model.ProductCosts;

@Entity
@Table(name = "tbl_biogas_plant_boilers")
public class BiogasPlantBoiler extends AbstractEntity {

	@OneToOne
	@JoinColumn(name = "f_boiler")
	public Boiler boiler;

	@Embedded
	public ProductCosts costs = new ProductCosts();

	@Override
	public BiogasPlantBoiler copy() {
		var clone = new BiogasPlantBoiler();
		clone.id = UUID.randomUUID().toString();
		clone.boiler = boiler;
		clone.costs = costs != null ? costs.copy() : new ProductCosts();
		return clone;
	}
}
