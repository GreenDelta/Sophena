package sophena.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

/**
 * The fuel specification of a heat producer.
 */
@Embeddable
public class FuelSpec {

	@OneToOne
	@JoinColumn(name = "f_fuel")
	public Fuel fuel;

	/**
	 * This must be set if the fuel is a wood fuel otherwise it must be null.
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "wood_amount_type")
	public WoodAmountType woodAmountType;

	@Column(name = "water_content")
	public double waterContent;

	@Column(name = "price_per_unit")
	public double pricePerUnit;

	@Column(name = "tax_rate")
	public double taxRate;

	@Override
	public FuelSpec clone() {
		FuelSpec clone = new FuelSpec();
		clone.fuel = fuel;
		clone.woodAmountType = woodAmountType;
		clone.waterContent = waterContent;
		clone.pricePerUnit = pricePerUnit;
		clone.taxRate = taxRate;
		return clone;
	}
}
