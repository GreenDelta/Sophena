package sophena.model;

import java.util.UUID;

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
public class Boiler extends AbstractProduct {

	@Column(name = "max_power")
	public double maxPower;

	@Column(name = "min_power")
	public double minPower;

	/**
	 * If the boiler is a wood fuel boiler this field is <code>null</code> and
	 * the woodAmountType is set.
	 */
	@OneToOne
	@JoinColumn(name = "f_fuel")
	public Fuel fuel;

	@Column(name = "efficiency_rate")
	public double efficiencyRate;

	/**
	 * If the boiler is a wood fuel boiler this field must be set; otherwise it
	 * is <code>null</code>.
	 */
	@Enumerated(EnumType.STRING)
	@Column(name = "wood_amount_type")
	public WoodAmountType woodAmountType;

	@Column(name = "is_co_gen_plant")
	public boolean isCoGenPlant;

	@Column(name = "max_power_electric")
	public double maxPowerElectric;

	@Column(name = "min_power_electric")
	public double minPowerElectric;

	@Column(name = "efficiency_rate_electric")
	public double efficiencyRateElectric;

	@Override
	public Boiler clone() {
		Boiler clone = new Boiler();
		AbstractProduct.copyFields(this, clone);
		clone.id = UUID.randomUUID().toString();
		clone.maxPower = maxPower;
		clone.minPower = minPower;
		clone.fuel = fuel;
		clone.efficiencyRate = efficiencyRate;
		clone.woodAmountType = woodAmountType;
		clone.isCoGenPlant = isCoGenPlant;
		clone.maxPowerElectric = maxPowerElectric;
		clone.minPowerElectric = minPowerElectric;
		clone.efficiencyRateElectric = efficiencyRateElectric;
		return clone;
	}
}
