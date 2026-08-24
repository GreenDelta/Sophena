package sophena.calc.biogas.fermentersim;

/// Constant values of the fermenter simulation.
///
/// @param k0C                 Celsius-Kelvin offset in K
/// Parameter of the original fermenter simulation model: `k0C`.
/// @param sigma               Stefan-Boltzmann constant in W/(m2K4)
/// Parameter of the original fermenter simulation model: `sigma`.
/// @param g                   Standard acceleration of gravity in m/s2
/// Parameter of the original fermenter simulation model: `g`.
/// @param solarConstant       Total Solar Irradiance constant in W/m2
/// Parameter of the original fermenter simulation model: `solarConstantWm2`.
/// @param reTransition        Critical Reynolds transition number
/// Parameter of the original fermenter simulation model: `reTransition`.
/// @param soilBuffer          Soil buffer depth in m
/// Parameter of the original fermenter simulation model: `soilBufferM`.
/// @param methaneHeatingValue Methane lower heating value in kWh/Nm3
/// Parameter of the original fermenter simulation model: `methaneLowerHeatingValueKwhNm3`.
/// @param normalTemperatureK  Normal temperature in K
/// Parameter of the original fermenter simulation model: `normalTemperatureK`.
/// @param skyEmissivity       Effective sky emissivity
/// Parameter of the original fermenter simulation model: `skyEmissivity`.
/// @param tolerance           Convergence tolerance for temperature in K
/// Parameter of the original fermenter simulation model: `toleranceK`.
/// @param residualTolerance   Convergence residual tolerance in W
/// Parameter of the original fermenter simulation model: `residualToleranceW`.
/// @param relaxation          Under-relaxation factor (0 < omega <= 1)
/// Parameter of the original fermenter simulation model: `relaxation`.
/// @param maxIterations       Maximum iterations per timestep
/// Parameter of the original fermenter simulation model: `maxIterations`.
public record FermenterConstants(
	double k0C,
	double sigma,
	double g,
	double solarConstant,
	double reTransition,
	double soilBuffer,
	double methaneHeatingValue,
	double normalTemperatureK,
	double skyEmissivity,
	double tolerance,
	double residualTolerance,
	double relaxation,
	int maxIterations
) {
	public static FermenterConstants createDefault() {
		return new FermenterConstants(
			273.15,            // Celsius-Kelvin offset [K]
			5.670374419e-8,    // Stefan-Boltzmann constant [W/(m2 K4)]
			9.80665,           // Standard acceleration of gravity [m/s2]
			1361.0,            // Total Solar Irradiance constant [W/m2]
			5e5,               // Critical Reynolds transition number [-]
			2.5,               // Soil buffer depth [m]
			9.97,              // Methane lower heating value [kWh/Nm3]
			273.15,            // Normal temperature [K]
			0.80,              // Effective sky emissivity [-]
			0.002,             // Convergence tolerance for temperature [K]
			1.0,               // Convergence residual tolerance [W]
			0.5,               // Under-relaxation factor (0 < omega <= 1) [-]
			100                // Maximum iterations per timestep
		);
	}
}
