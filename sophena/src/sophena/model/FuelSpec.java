package sophena.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * The fuel specification of a heat producer. If the boiler is using a wood fuel
 * this fuel is specified here, otherwise it is taken from the boiler. Also the
 * water content is only valid for wood fuels.
 */
@Embeddable
public class FuelSpec {

	@OneToOne
	@JoinColumn(name = "f_wood_fuel")
	private Fuel woodFuel;

	@Column(name = "water_content")
	private double waterContent;

	@Column(name = "price_per_unit")
	private double pricePerUnit;

	@Column(name = "tax_rate")
	private double taxRate;

	public Fuel getWoodFuel() {
		return woodFuel;
	}

	public void setWoodFuel(Fuel woodFuel) {
		this.woodFuel = woodFuel;
	}

	public double getWaterContent() {
		return waterContent;
	}

	public void setWaterContent(double waterContent) {
		this.waterContent = waterContent;
	}

	public double getPricePerUnit() {
		return pricePerUnit;
	}

	public void setPricePerUnit(double pricePerUnit) {
		this.pricePerUnit = pricePerUnit;
	}

	public double getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(double taxRate) {
		this.taxRate = taxRate;
	}

	@Override
	public FuelSpec clone() {
		FuelSpec clone = new FuelSpec();
		clone.setPricePerUnit(getPricePerUnit());
		clone.setTaxRate(getTaxRate());
		clone.setWaterContent(getWaterContent());
		clone.setWoodFuel(getWoodFuel());
		return clone;
	}

}
