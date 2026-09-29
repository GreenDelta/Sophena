package sophena.model.biogas;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import sophena.model.AnnualCostEntry;
import sophena.model.Fuel;
import sophena.model.ProductGroup;
import sophena.model.RootEntity;

@Entity
@Table(name = "tbl_biogas_plants")
public class BiogasPlant extends RootEntity {

	/// The duration of the biogas plant in years. This can be different
	/// from the duration of the project in which the biogas plant is used.
	@Column(name = "plant_duration")
	public int duration;

	@OneToOne
	@JoinColumn(name = "f_produced_electricity")
	public Fuel producedElectricity;

	@OneToOne
	@JoinColumn(name = "f_product_group")
	public ProductGroup productGroup;

	@OneToOne
	@JoinColumn(name = "f_electricity_price_curve")
	public ElectricityPriceCurve electricityPrices;

	/// The settings of the plant, e.g. the average hourly wage or the
	/// electricity price. A plant is always created with a settings instance.
	@OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_settings")
	public BiogasPlantSettings settings;

	/// Configured size of the gas storage in m3.
	@Column(name = "gas_storage_size")
	public double gasStorageSize;

	/// minimum runtime in hours
	@Column(name = "minimum_runtime")
	public int minimumRuntime;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "f_fermenter")
	public Fermenter fermenter;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_biogas_plant")
	public final List<BiogasPlantBoiler> boilers = new ArrayList<>();

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_biogas_plant")
	public final List<SubstrateProfile> substrateProfiles = new ArrayList<>();

	/// Insurance costs as a percentage of investment.
	@Column(name = "insurance_share")
	public double insuranceShare;

	/// Other annual costs in EUR/a, such as administration costs, laboratory costs, etc.
	@ElementCollection
	@CollectionTable(
		name = "tbl_biogas_annual_costs",
		joinColumns = @JoinColumn(name = "f_biogas_plant")
	)
	public List<AnnualCostEntry> otherAnnualCosts = new ArrayList<>();

	// price change factors

	/// Price change factor for investments.
	@Column(name = "investment_factor")
	public double investmentFactor;

	/// Price change factor for biomass fuels.
	@Column(name = "bio_fuel_factor")
	public double bioFuelFactor;

	/// Price change factor for fossil fuels.
	@Column(name = "fossil_fuel_factor")
	public double fossilFuelFactor;

	/// Price change factor for electricity.
	@Column(name = "electricity_factor")
	public double electricityFactor;

	/// Price change factor for wages/operation.
	@Column(name = "operation_factor")
	public double operationFactor;

	/// Price change factor for maintenance.
	@Column(name = "maintenance_factor")
	public double maintenanceFactor;

	/// Price change factor for electricity revenues.
	@Column(name = "electricity_revenues_factor")
	public double electricityRevenuesFactor;

	@Override
	public BiogasPlant copy() {
		var copy = new BiogasPlant();
		copy.id = UUID.randomUUID().toString();
		copy.name = name;
		copy.description = description;
		copy.duration = duration;
		copy.producedElectricity = producedElectricity;
		copy.productGroup = productGroup;
		copy.electricityPrices = electricityPrices;
		copy.gasStorageSize = gasStorageSize;
		copy.minimumRuntime = minimumRuntime;
		copy.fermenter = fermenter != null ? fermenter.copy() : null;
		for (var boiler : boilers) {
			if (boiler != null) {
				copy.boilers.add(boiler.copy());
			}
		}
		for (var p : substrateProfiles) {
			copy.substrateProfiles.add(p.copy());
		}
		copy.settings = settings != null
			? settings.copy()
			: BiogasPlantSettings.createDefault(null);
		copy.insuranceShare = insuranceShare;
		for (var entry : otherAnnualCosts) {
			if (entry != null) {
				copy.otherAnnualCosts.add(entry.copy());
			}
		}
		copy.investmentFactor = investmentFactor;
		copy.bioFuelFactor = bioFuelFactor;
		copy.fossilFuelFactor = fossilFuelFactor;
		copy.electricityFactor = electricityFactor;
		copy.operationFactor = operationFactor;
		copy.maintenanceFactor = maintenanceFactor;
		copy.electricityRevenuesFactor = electricityRevenuesFactor;
		return copy;
	}

}
