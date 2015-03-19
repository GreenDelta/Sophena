package sophena.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_fuel_consumptions")
public class FuelConsumption extends AbstractEntity {

	@OneToOne
	@JoinColumn(name = "f_fuel")
	private Fuel fuel;

	@Column(name = "amount")
	private double amount;

	@Column(name = "utilisation_rate")
	private double utilisationRate;

	// only valid for wood fuels
	@Enumerated(EnumType.STRING)
	@Column(name = "wood_amount_type")
	private WoodAmountType woodAmountType;

	// only valid for wood fuels
	@Column(name = "water_content")
	private double waterContent;

	public Fuel getFuel() {
		return fuel;
	}

	public void setFuel(Fuel fuel) {
		this.fuel = fuel;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public double getUtilisationRate() {
		return utilisationRate;
	}

	public void setUtilisationRate(double utilisationRate) {
		this.utilisationRate = utilisationRate;
	}

	public WoodAmountType getWoodAmountType() {
		return woodAmountType;
	}

	public void setWoodAmountType(WoodAmountType woodAmountType) {
		this.woodAmountType = woodAmountType;
	}

	public double getWaterContent() {
		return waterContent;
	}

	public void setWaterContent(double waterContent) {
		this.waterContent = waterContent;
	}

	@Override
	public FuelConsumption clone() {
		FuelConsumption clone = new FuelConsumption();
		clone.setId(UUID.randomUUID().toString());
		clone.setAmount(this.getAmount());
		clone.setFuel(this.getFuel());
		clone.setUtilisationRate(this.getUtilisationRate());
		clone.setWaterContent(this.getWaterContent());
		clone.setWoodAmountType(this.getWoodAmountType());
		return clone;
	}
}
