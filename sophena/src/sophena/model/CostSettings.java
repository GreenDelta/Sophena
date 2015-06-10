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

	@Column(name = "interest_rate")
	private double interestRate;

	@Column(name = "interest_rate_funding")
	private double interestRateFunding;

	@Column(name = "investment_factor")
	private double investmentFactor;

	@Column(name = "operation_factor")
	private double operationFactor;

	@Column(name = "hourly_wage")
	private double hourlyWage;

	@Column(name = "bio_fuel_factor")
	private double bioFuelFactor;

	@Column(name = "fossil_fuel_factor")
	private double fossilFuelFactor;

	@Column(name = "electricity_factor")
	private double electricityFactor;

	@Column(name = "electricity_price")
	private double electricityPrice;

	@Column(name = "maintenance_factor")
	private double maintenanceFactor;

	@Column(name = "vat_rate")
	private double vatRate;

	@Column(name = "insurance_share")
	private double insuranceShare;

	@Column(name = "other_share")
	private double otherShare;

	@Column(name = "administration_share")
	private double administrationShare;

	public double getInterestRate() {
		return interestRate;
	}

	public void setInterestRate(double interestRate) {
		this.interestRate = interestRate;
	}

	public double getInterestRateFunding() {
		return interestRateFunding;
	}

	public void setInterestRateFunding(double interestRateFunding) {
		this.interestRateFunding = interestRateFunding;
	}

	public double getInvestmentFactor() {
		return investmentFactor;
	}

	public void setInvestmentFactor(double investmentFactor) {
		this.investmentFactor = investmentFactor;
	}

	public double getOperationFactor() {
		return operationFactor;
	}

	public void setOperationFactor(double operationFactor) {
		this.operationFactor = operationFactor;
	}

	public double getHourlyWage() {
		return hourlyWage;
	}

	public void setHourlyWage(double hourlyWage) {
		this.hourlyWage = hourlyWage;
	}

	public double getBioFuelFactor() {
		return bioFuelFactor;
	}

	public void setBioFuelFactor(double bioFuelFactor) {
		this.bioFuelFactor = bioFuelFactor;
	}

	public double getFossilFuelFactor() {
		return fossilFuelFactor;
	}

	public void setFossilFuelFactor(double fossilFuelFactor) {
		this.fossilFuelFactor = fossilFuelFactor;
	}

	public double getElectricityPrice() {
		return electricityPrice;
	}

	public void setElectricityPrice(double electricityPrice) {
		this.electricityPrice = electricityPrice;
	}

	public double getElectricityFactor() {
		return electricityFactor;
	}

	public void setElectricityFactor(double electricityFactor) {
		this.electricityFactor = electricityFactor;
	}

	public double getMaintenanceFactor() {
		return maintenanceFactor;
	}

	public void setMaintenanceFactor(double maintenanceFactor) {
		this.maintenanceFactor = maintenanceFactor;
	}

	public double getVatRate() {
		return vatRate;
	}

	public void setVatRate(double vatRate) {
		this.vatRate = vatRate;
	}

	public double getInsuranceShare() {
		return insuranceShare;
	}

	public void setInsuranceShare(double insuranceShare) {
		this.insuranceShare = insuranceShare;
	}

	public double getOtherShare() {
		return otherShare;
	}

	public void setOtherShare(double otherShare) {
		this.otherShare = otherShare;
	}

	public double getAdministrationShare() {
		return administrationShare;
	}

	public void setAdministrationShare(double administrationShare) {
		this.administrationShare = administrationShare;
	}

	@Override
	public CostSettings clone() {
		CostSettings clone = new CostSettings();
		clone.setId(UUID.randomUUID().toString());
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
