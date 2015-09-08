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
	public Fuel woodFuel;

	@Column(name = "water_content")
	public double waterContent;

	@Column(name = "price_per_unit")
	public double pricePerUnit;

	@Column(name = "tax_rate")
	public double taxRate;

	@Override
	public FuelSpec clone() {
		FuelSpec clone = new FuelSpec();
		clone.pricePerUnit = pricePerUnit;
		clone.taxRate = taxRate;
		clone.waterContent = waterContent;
		clone.woodFuel = woodFuel;
		return clone;
	}
}
