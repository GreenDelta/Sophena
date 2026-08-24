package sophena.calc.biogas.fermentersim;

/**
 * Physical constants and solver iteration parameters.
 */
public record FermenterConstants(
	double k0C,
	double sigma,
	double g,
	double solarConstantWm2,
	double reTransition,
	double soilBufferM,
	double methaneLowerHeatingValueKwhNm3,
	double normalTemperatureK,
	double skyEmissivity,
	double toleranceK,
	double residualToleranceW,
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
