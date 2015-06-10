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

	@Column(name = "is_global")
	private boolean global;

	@Column(name = "investment_factor")
	private double investmentFactor;

	@Column(name = "other_factor")
	private double otherFactor;

	@Column(name = "hourly_wage")
	private double hourlyWage;

	@Column(name = "bio_fuel_factor")
	private double bioFuelFactor;

	@Column(name = "fossil_fuel_factor")
	private double fossilFuelFactor;

	@Column(name = "electricity_factor")
	private double electricityFactor;

	@Column(name = "maintenance_factor")
	private double maintenanceFactor;

	@Column(name = "vat_rate")
	private double vatRate;

	@Column(name = "insurance_share")
	private double insuranceShare;

	@Column(name = "tax_share")
	private double taxShare;

	public boolean isGlobal() {
		return global;
	}

	public void setGlobal(boolean global) {
		this.global = global;
	}

	public double getInvestmentFactor() {
		return investmentFactor;
	}

	public void setInvestmentFactor(double investmentFactor) {
		this.investmentFactor = investmentFactor;
	}

	public double getOtherFactor() {
		return otherFactor;
	}

	public void setOtherFactor(double otherFactor) {
		this.otherFactor = otherFactor;
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

	public double getTaxShare() {
		return taxShare;
	}

	public void setTaxShare(double taxShare) {
		this.taxShare = taxShare;
	}

	@Override
	protected CostSettings clone()  {
		CostSettings clone = new CostSettings();
		clone.setId(UUID.randomUUID().toString());
		clone.investmentFactor = investmentFactor;
		clone.otherFactor = otherFactor;
		clone.hourlyWage = hourlyWage;
		clone.bioFuelFactor = bioFuelFactor;
		clone.fossilFuelFactor = fossilFuelFactor;
		clone.electricityFactor = electricityFactor;
		clone.maintenanceFactor = maintenanceFactor;
		clone.vatRate = vatRate;
		clone.insuranceShare = insuranceShare;
		clone.taxShare = taxShare;
		return clone;
	}
}
