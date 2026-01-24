package sophena.model.biogas;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import sophena.model.AnnualCostEntry;
import sophena.model.Boiler;
import sophena.model.Fuel;
import sophena.model.ProductCosts;
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

	/// Average hourly wage in EUR.
	@Column(name = "hourly_wage")
	public double hourlyWage;

	/// Price for purchased electricity in EUR/kWh when operating in full
	/// feed-in mode or when the CHP unit is idle.
	@Column(name = "electricity_price")
	public double electricityPrice;

	/// The self-consumption of electricity in kW. This is subtracted from
	/// electricity production and not considered for grid feed-in. When no
	/// electricity is produced, the self-consumption must be sourced from
	/// other sources like the power grid.
	@Column(name = "electricity_demand")
	public double electricityDemand;

	/// The electricity mix used for self-consumption when the biogas plant
	/// is idle or when operating in full feed-in mode.
	@OneToOne
	@JoinColumn(name = "f_demand_electricity_mix")
	public Fuel demandElectricityMix;

	/// Indicates whether the plant operates in full feed-in mode. In full feed-in
	/// mode, the electricity price above is always used for self-consumption. In
	/// surplus feed-in mode, the self-consumption is subtracted from the installed
	/// capacity when the CHP is running and not fed into the grid; at other times,
	/// the electricity price above is used.
	@Column(name = "is_full_feed_in")
	public boolean isFullFeedIn;

	/// Cable and transformer losses in kW. Transformer losses are typically 0.7%
	/// of electricity production; cable losses depend on voltage, power, and cable
	/// length. At medium voltage, losses are negligible.
	@Column(name = "transmission_losses")
	public double transmissionLosses;

	/// Heat losses in kW that may occur before feeding into the heat network.
	/// Heat losses within the heat network are accounted for elsewhere.
	@Column(name = "heat_loss")
	public double heatLoss;

	/// Capital mixed interest rate in %.
	@Column(name = "interest_rate")
	public double interestRate;

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
		var clone = new BiogasPlant();
		clone.id = UUID.randomUUID().toString();
		clone.name = name;
		clone.description = description;
		clone.duration = duration;
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
		clone.hourlyWage = hourlyWage;
		clone.electricityPrice = electricityPrice;
		clone.electricityDemand = electricityDemand;
		clone.demandElectricityMix = demandElectricityMix;
		clone.isFullFeedIn = isFullFeedIn;
		clone.transmissionLosses = transmissionLosses;
		clone.heatLoss = heatLoss;
		clone.interestRate = interestRate;
		clone.insuranceShare = insuranceShare;
		for (var entry : otherAnnualCosts) {
			if (entry != null) {
				clone.otherAnnualCosts.add(entry.copy());
			}
		}
		clone.investmentFactor = investmentFactor;
		clone.bioFuelFactor = bioFuelFactor;
		clone.fossilFuelFactor = fossilFuelFactor;
		clone.electricityFactor = electricityFactor;
		clone.operationFactor = operationFactor;
		clone.maintenanceFactor = maintenanceFactor;
		clone.electricityRevenuesFactor = electricityRevenuesFactor;
		return clone;
	}
}
