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
public class Boiler extends Product {

	@Column(name = "max_power")
	public double maxPower;

	@Column(name = "min_power")
	public double minPower;

	@OneToOne
	@JoinColumn(name = "f_fuel")
	public Fuel fuel;

	@Column(name = "efficiency_rate")
	public double efficiencyRate;

	@Enumerated(EnumType.STRING)
	@Column(name = "wood_amount_type")
	public WoodAmountType woodAmountType;
}
