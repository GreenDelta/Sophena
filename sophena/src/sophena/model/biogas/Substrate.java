package sophena.model.biogas;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import sophena.model.BaseDataEntity;

@Entity
@Table(name = "tbl_biogas_substrates")
public class Substrate extends BaseDataEntity {

	/// dry matter content in percentage (%)
	@Column(name = "dry_matter")
	public double dryMatter;

	/// organic dry matter content in percentage (%)
	@Column(name = "organic_dry_matter")
	public double organicDryMatter;

	/// biogas production in m³/t of organic dry matter
	@Column(name = "biogas_production")
	public double biogasProduction;

	/// methane content of the produced biogas in percentage %
	@Column(name = "methane_content")
	public double methaneContent;

	/// CO2 emissions in g CO2 eq./kWh
	@Column(name = "co2_emissions")
	public double co2Emissions;

	@Override
	public Substrate copy() {
		var clone = new Substrate();
		clone.id = UUID.randomUUID().toString();
		clone.name = name;
		clone.description = description;
		clone.isProtected = isProtected;
		clone.dryMatter = dryMatter;
		clone.organicDryMatter = organicDryMatter;
		clone.biogasProduction = biogasProduction;
		clone.methaneContent = methaneContent;
		clone.co2Emissions = co2Emissions;
		return clone;
	}
}
