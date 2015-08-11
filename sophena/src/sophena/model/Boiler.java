package sophena.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * If the boiler is for wood fuels the field <code>fuel</code> must be
 * <code>null</code> and the field <code>woodAmountType</code> must be set.
 */
@Entity
@Table(name = "tbl_boilers")
public class Boiler extends RootEntity {

	@Column(name = "purchase_price")
	private Double purchasePrice;

	@Column(name = "url")
	private String url;

	@Column(name = "max_power")
	private double maxPower;

	@Column(name = "min_power")
	private double minPower;

	@OneToOne
	@JoinColumn(name = "f_fuel")
	private Fuel fuel;

	@Column(name = "efficiency_rate")
	private double efficiencyRate;

	@Enumerated(EnumType.STRING)
	@Column(name = "wood_amount_type")
	private WoodAmountType woodAmountType;

	public Double getPurchasePrice() {
		return purchasePrice;
	}

	public void setPurchasePrice(Double purchasePrice) {
		this.purchasePrice = purchasePrice;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public double getMaxPower() {
		return maxPower;
	}

	public void setMaxPower(double maxPower) {
		this.maxPower = maxPower;
	}

	public double getMinPower() {
		return minPower;
	}

	public void setMinPower(double minPower) {
		this.minPower = minPower;
	}

	public Fuel getFuel() {
		return fuel;
	}

	public void setFuel(Fuel fuel) {
		this.fuel = fuel;
	}

	public double getEfficiencyRate() {
		return efficiencyRate;
	}

	public void setEfficiencyRate(double efficiencyRate) {
		this.efficiencyRate = efficiencyRate;
	}

	public WoodAmountType getWoodAmountType() {
		return woodAmountType;
	}

	public void setWoodAmountType(WoodAmountType woodAmountType) {
		this.woodAmountType = woodAmountType;
	}

	public boolean isForWood() {
		return woodAmountType != null;
	}
}
