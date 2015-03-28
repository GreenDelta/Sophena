package sophena.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_boilers")
public class Boiler extends RootEntity {

	@Column(name = "tbl_purchase_price")
	private Double purchasePrice;

	@Column(name = "tbl_url")
	private Double url;

	@Column(name = "tbl_max_power")
	private double maxPower;

	@Column(name = "tbl_min_power")
	private double minPower;

	@OneToOne
	@JoinColumn(name = "f_fuel")
	private Fuel fuel;

	@Column(name = "efficiency_rate")
	private double efficiencyRate;

	public Double getPurchasePrice() {
		return purchasePrice;
	}

	public void setPurchasePrice(Double purchasePrice) {
		this.purchasePrice = purchasePrice;
	}

	public Double getUrl() {
		return url;
	}

	public void setUrl(Double url) {
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
}
