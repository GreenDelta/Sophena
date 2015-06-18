package sophena.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 'Factor' in this class means 'price change factor'.
 */
@Entity
@Table(name = "tbl_cost_settings")
public class CostSettings extends AbstractEntity {

	public static final String GLOBAL_ID = "9ff7e5f9-c603-4f21-b687-22191b697ba1";

	@Column(name = "funding")
	public double funding;

	@Column(name = "electricity_revenues")
	public double electricityRevenues;

	@Column(name = "interest_rate_funding")
	public double interestRateFunding;

	@Column(name = "interest_rate")
	public double interestRate;

	@Column(name = "investment_factor")
	public double investmentFactor;

	@Column(name = "operation_factor")
	public double operationFactor;

	@Column(name = "hourly_wage")
	public double hourlyWage;

	@Column(name = "bio_fuel_factor")
	public double bioFuelFactor;

	@Column(name = "fossil_fuel_factor")
	public double fossilFuelFactor;

	@Column(name = "electricity_factor")
	public double electricityFactor;

	@Column(name = "electricity_price")
	public double electricityPrice;

	@Column(name = "maintenance_factor")
	public double maintenanceFactor;

	@Column(name = "vat_rate")
	public double vatRate;

	@Column(name = "insurance_share")
	public double insuranceShare;

	@Column(name = "other_share")
	public double otherShare;

	@Column(name = "administration_share")
	public double administrationShare;

	@Override
	public CostSettings clone() {
		CostSettings clone = new CostSettings();
		clone.setId(UUID.randomUUID().toString());
		clone.funding = funding;
		clone.electricityRevenues = electricityRevenues;
		clone.interestRate = interestRate;
		clone.interestRateFunding = interestRateFunding;
		clone.investmentFactor = investmentFactor;
		clone.operationFactor = operationFactor;
		clone.hourlyWage = hourlyWage;
		clone.bioFuelFactor = bioFuelFactor;
		clone.fossilFuelFactor = fossilFuelFactor;
		clone.electricityPrice = electricityPrice;
		clone.electricityFactor = electricityFactor;
		clone.maintenanceFactor = maintenanceFactor;
		clone.vatRate = vatRate;
		clone.insuranceShare = insuranceShare;
		clone.otherShare = otherShare;
		clone.administrationShare = administrationShare;
		return clone;
	}
}
