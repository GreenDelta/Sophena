package sophena;

public interface Defaults {

	/**
	 * Default factor for smoothing the project load curve when the simultanety
	 * factor is < 1.
	 */
	double SMOOTHING_FACTOR = 10;

	/** The emission factor for electricity in kg CO2 eq./kWh . */
	double EMISSION_FACTOR_ELECTRICITY = 0.6148;

	/** The emission factor for oil in kg CO2 eq./kWh . */
	double EMISSION_FACTOR_OIL = 0.3072;

	/** The emission factor for natural gas in kg CO2 eq./kWh . */
	double EMISSION_FACTOR_NATURAL_GAS = 0.2392;

	double PRIMARY_ENERGY_FACTOR_ELECTRICITY = 2.8;

	double SPECIFIC_STAND_BY_LOSS = 0.014;

}
