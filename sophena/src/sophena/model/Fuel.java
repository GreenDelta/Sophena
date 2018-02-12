package sophena.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_fuels")
public class Fuel extends BaseDataEntity {

	/**
	 * the standard unit of the fuel (e.g. L, m3, kg); for wood types this
	 * should be always kg
	 */
	@Column(name = "unit")
	public String unit;

	/**
	 * the calorific value in kWh per 1 standard unit, for wood fuels this field
	 * stores the calorific value for 1 kg, absolutely dry
	 */
	@Column(name = "calorific_value")
	public double calorificValue;

	/** only for wood fuels: density in kg per solid cubic meter */
	@Column(name = "density")
	public double density;

	/** indicates whether the fuel is a wood fuel */
	@Column(name = "is_wood")
	public boolean wood;

	/**
	 * Gramme CO2 emissions per kWh fuel energy.
	 */
	@Column(name = "co2_emissions")
	public double co2Emissions;

	@Column(name = "primary_energy_factor")
	public double primaryEnergyFactor;

	@Override
	public Fuel clone() {
		Fuel clone = new Fuel();
		clone.id = UUID.randomUUID().toString();
		clone.name = name;
		clone.description = description;
		clone.isProtected = isProtected;
		clone.unit = unit;
		clone.calorificValue = calorificValue;
		clone.density = density;
		clone.wood = wood;
		clone.co2Emissions = co2Emissions;
		clone.primaryEnergyFactor = primaryEnergyFactor;
		return clone;
	}

}
