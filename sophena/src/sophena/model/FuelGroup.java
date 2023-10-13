package sophena.model;

/**
 * Fuels with the same properties are grouped into fuel groups. For a heat
 * producer, a fuel can be only selected from the group that is defined in the
 * product group of the producer.
 */
public enum FuelGroup {
	BIOGAS,

	NATURAL_GAS,

	LIQUID_GAS,

	HEATING_OIL,

	PELLETS,

	ELECTRICITY,

	HOT_WATER,

	PLANTS_OIL,

	WOOD,

	WASTE
}
