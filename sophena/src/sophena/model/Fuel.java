package sophena.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_fuels")
public class Fuel extends BaseDataEntity {

	/** The standard unit of the fuel (e.g. L, m3, kg). */
	@Column(name = "unit")
	public String unit;

	/** The calorific value in kWh per 1 standard unit. */
	@Column(name = "calorific_value")
	public double calorificValue;

	/** Density of a wood fuel in kg per solid cubic meter. */
	@Column(name = "density")
	public double density;

	/** Each fuel belongs to a group with equal properties. */
	@Enumerated(EnumType.STRING)
	@Column(name = "fuel_group")
	public FuelGroup group;

	/** CO2 emissions in g/kWh. */
	@Column(name = "co2_emissions")
	public double co2Emissions;

	@Column(name = "primary_energy_factor")
	public double primaryEnergyFactor;

	/** The ash content (in %) for wood based fuels. */
	@Column(name = "ash_content")
	public double ashContent;

	public boolean isWood() {
		return group == FuelGroup.WOOD;
	}

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
		clone.group = group;
		clone.co2Emissions = co2Emissions;
		clone.primaryEnergyFactor = primaryEnergyFactor;
		clone.ashContent = ashContent;
		return clone;
	}

}
