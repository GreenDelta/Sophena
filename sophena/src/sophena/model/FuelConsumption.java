package sophena.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import sophena.math.energetic.CalorificValue;

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

	/**
	 * Returns the amount of heat that was really used based on the consumption
	 * data.
	 */
	public double getUsedHeat() {
		if (fuel == null)
			return 0;
		double ur = utilisationRate / 100;
		double cv = CalorificValue.get(this);
		return ur * (cv * amount);
	}

	@Override
	public FuelConsumption copy() {
		var clone = new FuelConsumption();
		clone.id = UUID.randomUUID().toString();
		clone.amount = this.amount;
		clone.fuel = this.fuel;
		clone.utilisationRate = this.utilisationRate;
		clone.waterContent = this.waterContent;
		clone.woodAmountType = this.woodAmountType;
		return clone;
	}
}
