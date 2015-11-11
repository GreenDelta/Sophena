package sophena.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import sophena.math.energetic.FuelEnergy;

@Entity
@Table(name = "tbl_fuel_consumptions")
public class FuelConsumption extends AbstractEntity {

	@OneToOne
	@JoinColumn(name = "f_fuel")
	public Fuel fuel;

	@Column(name = "amount")
	public double amount;

	@Column(name = "utilisation_rate")
	public double utilisationRate;

	/** only valid for wood fuels */
	@Enumerated(EnumType.STRING)
	@Column(name = "wood_amount_type")
	public WoodAmountType woodAmountType;

	/** only valid for wood fuels */
	@Column(name = "water_content")
	public double waterContent;

	public double getUsedHeat() {
		if (fuel == null)
			return 0;
		double ur = utilisationRate / 100;
		if (!fuel.wood) {
			return ur * FuelEnergy
					.ofAmount_unit(amount)
					.calorificValue_kWh_per_unit(fuel.calorificValue)
					.get_kWh();
		}
		double mass;
		double wc = waterContent / 100;
		if (woodAmountType == null || woodAmountType == WoodAmountType.MASS)
			mass = amount;
		else {
			mass = amount * woodAmountType.getFactor() * fuel.density / (1 - wc);
		}
		// 0.68: vaporization enthalpy of water
		double heat = mass * ((1 - wc) * fuel.calorificValue - wc * 0.68);
		return ur * heat;
	}

	@Override
	public FuelConsumption clone() {
		FuelConsumption clone = new FuelConsumption();
		clone.id = UUID.randomUUID().toString();
		clone.amount = this.amount;
		clone.fuel = this.fuel;
		clone.utilisationRate = this.utilisationRate;
		clone.waterContent = this.waterContent;
		clone.woodAmountType = this.woodAmountType;
		return clone;
	}
}
