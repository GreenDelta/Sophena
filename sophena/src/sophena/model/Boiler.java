package sophena.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_boilers")
public class Boiler extends AbstractProduct {

	@Column(name = "max_power")
	public double maxPower;

	@Column(name = "min_power")
	public double minPower;

	@Column(name = "efficiency_rate")
	public double efficiencyRate;

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
		clone.efficiencyRate = efficiencyRate;
		clone.isCoGenPlant = isCoGenPlant;
		clone.maxPowerElectric = maxPowerElectric;
		clone.minPowerElectric = minPowerElectric;
		clone.efficiencyRateElectric = efficiencyRateElectric;
		return clone;
	}
}
