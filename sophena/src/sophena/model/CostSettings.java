package sophena.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * 'Factor' in this class means 'price change factor'.
 */
@Entity
@Table(name = "tbl_cost_settings")
public class CostSettings extends AbstractEntity {

	public static final String GLOBAL_ID = "9ff7e5f9-c603-4f21-b687-22191b697ba1";

	// general costs

	@Column(name = "hourly_wage")
	public double hourlyWage;

	@Column(name = "electricity_price")
	public double electricityPrice;

	/**
	 * This value is used to calculate the amount of electricity that is
	 * required for an amount of produced heat. It is a percentage value, 1.5%.
	 */
	@Column(name = "electricity_demand_share")
	public double electricityDemandShare;

	/**
	 * The electricity that is used for the electricity consumption of the heat
	 * producers in a project.
	 */
	@OneToOne
	@JoinColumn(name = "f_project_electricity_mix")
	public Fuel projectElectricityMix;

	/**
	 * The default setting of the general electricity mix..
	 */
	@OneToOne
	@JoinColumn(name = "f_electricity_mix")
	public Fuel electricityMix;

	/**
	 * The default setting of the (fossil fuel based) electricity mix that is
	 * replaced by electricity from renewable energy sources.
	 */
	@OneToOne
	@JoinColumn(name = "f_replaced_electricity_mix")
	public Fuel replacedElectricityMix;

	/** Average revenues from generated electricity in EUR/kWh */
	@Column(name = "electricity_revenues")
	public double electricityRevenues;

	/** Average revenues from generated heat in EUR/MWh. */
	@Column(name = "heat_revenues")
	public double heatRevenues;

	// financial aspects

	@Column(name = "interest_rate")
	public double interestRate;

	@Column(name = "interest_rate_funding")
	public double interestRateFunding;

	@Column(name = "connection_fees")
	public double connectionFees;
	
	/** General investment funding in EUR */
	@Column(name = "funding")
	public double funding;

	@Column(name = "funding_percent")
	public double fundingPercent;
	
	@Column(name = "funding_types")
	public int fundingTypes;

	// other costs

	@Column(name = "insurance_share")
	public double insuranceShare;

	@Column(name = "other_share")
	public double otherShare;

	@Column(name = "administration_share")
	public double administrationShare;

	@ElementCollection
	@CollectionTable(name = "tbl_annual_costs", joinColumns = @JoinColumn(name = "f_project"))
	public List<AnnualCostEntry> annualCosts = new ArrayList<>();

	// price change factors

	@Column(name = "investment_factor")
	public double investmentFactor;

	@Column(name = "bio_fuel_factor")
	public double bioFuelFactor;

	@Column(name = "fossil_fuel_factor")
	public double fossilFuelFactor;

	@Column(name = "electricity_factor")
	public double electricityFactor;

	@Column(name = "operation_factor")
	public double operationFactor;

	@Column(name = "maintenance_factor")
	public double maintenanceFactor;

	@Column(name = "heat_revenues_factor")
	public double heatRevenuesFactor;

	@Column(name = "electricity_revenues_factor")
	public double electricityRevenuesFactor;

	@Override
	public CostSettings copy() {
		var clone = new CostSettings();
		clone.id = UUID.randomUUID().toString();

		// fundings
		clone.funding = funding;
		clone.fundingPercent = fundingPercent;
		clone.fundingTypes = fundingTypes;

		// revenues
		clone.electricityRevenues = electricityRevenues;
		clone.heatRevenues = heatRevenues;

		clone.electricityDemandShare = electricityDemandShare;
		clone.projectElectricityMix = projectElectricityMix;
		clone.electricityMix = electricityMix;
		clone.replacedElectricityMix = replacedElectricityMix;
		clone.interestRate = interestRate;
		clone.interestRateFunding = interestRateFunding;
		clone.investmentFactor = investmentFactor;
		clone.operationFactor = operationFactor;
		clone.hourlyWage = hourlyWage;

		// price change factors
		clone.bioFuelFactor = bioFuelFactor;
		clone.fossilFuelFactor = fossilFuelFactor;
		clone.electricityPrice = electricityPrice;
		clone.electricityFactor = electricityFactor;
		clone.maintenanceFactor = maintenanceFactor;
		clone.heatRevenuesFactor = heatRevenuesFactor;
		clone.electricityRevenuesFactor = electricityRevenuesFactor;

		clone.insuranceShare = insuranceShare;
		clone.otherShare = otherShare;
		clone.administrationShare = administrationShare;
		clone.connectionFees = connectionFees;
		for (AnnualCostEntry ace : annualCosts) {
			if (ace != null) {
				clone.annualCosts.add(ace.copy());
			}
		}
		return clone;
	}
}
