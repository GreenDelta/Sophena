package sophena.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_fuels")
public class Fuel extends RootEntity {

	// the standard unit of the fuel (e.g. L, m3, kg)
	@Column(name = "unit")
	private String unit;

	// the calorific value in kWh per 1 standard unit, for wood fuels this field
	// stores the calorific value for 1 kg, absolutely dry
	@Column(name = "calorific_value")
	private double calorificValue;

    // only for wood fuels
	@Enumerated(EnumType.STRING)
	@Column(name = "wood_type")
	private WoodType woodType;

	// only for wood fuels: density in kg per solid cubic meter
	@Column(name = "density")
	private double density;

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public double getCalorificValue() {
		return calorificValue;
	}

	public void setCalorificValue(double calorificValue) {
		this.calorificValue = calorificValue;
	}

	public WoodType getWoodType() {
		return woodType;
	}

	public void setWoodType(WoodType woodType) {
		this.woodType = woodType;
	}

	public double getDensity() {
		return density;
	}

	public void setDensity(double density) {
		this.density = density;
	}
}
