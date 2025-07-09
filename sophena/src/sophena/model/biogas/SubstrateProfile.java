package sophena.model.biogas;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import sophena.model.AbstractEntity;
import sophena.model.DoubleArrayConverter;
import sophena.model.Stats;

@Entity
@Table(name = "tbl_biogas_substrate_profiles")
public class SubstrateProfile extends AbstractEntity {

	@OneToOne
	@JoinColumn(name = "f_substrate")
	public Substrate substrate;

	@Column(name = "annual_mass")
	public double annualMass; // in t/a

	@Column(name = "substrate_costs")
	public double substrateCosts; // in €/t

	@Column(name = "monthly_percentages")
	@Convert(converter = DoubleArrayConverter.class)
	public double[] monthlyPercentages; // 12 values

	@Column(name = "hourly_values")
	@Convert(converter = DoubleArrayConverter.class)
	public double[] hourlyValues; // 8760 values

	@Override
	public SubstrateProfile copy() {
		var clone = new SubstrateProfile();
		clone.id = UUID.randomUUID().toString();
		clone.substrate = substrate;
		clone.annualMass = annualMass;
		clone.substrateCosts = substrateCosts;
		clone.monthlyPercentages = Stats.copy(monthlyPercentages);
		clone.hourlyValues = Stats.copy(hourlyValues);
		return clone;
	}
}
