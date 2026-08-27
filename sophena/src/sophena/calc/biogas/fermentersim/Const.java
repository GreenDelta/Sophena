package sophena.calc.biogas.fermentersim;

/// Constant values of the fermenter simulation.
interface Const {

	/// Celsius-Kelvin offset in K.
	/// Parameter of the original fermenter simulation model: `k0C`.
	double k0C = 273.15;

	/// Stefan-Boltzmann constant in W/(m2K4).
	/// Parameter of the original fermenter simulation model: `sigma`.
	double sigma = 5.670374419e-8;

	/// Standard acceleration of gravity in m/s2.
	/// Parameter of the original fermenter simulation model: `g`.
	double g = 9.80665;

	/// Total Solar Irradiance constant in W/m2.
	/// Parameter of the original fermenter simulation model: `solarConstantWm2`.
	double solarConstant = 1361.0;

	/// Critical Reynolds transition number.
	/// Parameter of the original fermenter simulation model: `reTransition`.
	double reTransition = 5e5;

	/// Soil buffer depth in m.
	/// Parameter of the original fermenter simulation model: `soilBufferM`.
	double soilBuffer = 2.5;

	/// Methane lower heating value in kWh/Nm3.
	/// Parameter of the original fermenter simulation model: `methaneLowerHeatingValueKwhNm3`.
	double methaneHeatingValue = 9.97;

	/// Normal temperature in K.
	/// Parameter of the original fermenter simulation model: `normalTemperatureK`.
	double normalTemperatureK = 273.15;

	/// Effective sky emissivity.
	/// Parameter of the original fermenter simulation model: `skyEmissivity`.
	double skyEmissivity = 0.80;

	/// Convergence tolerance for temperature in K.
	/// Parameter of the original fermenter simulation model: `toleranceK`.
	double tolerance = 0.002;

	/// Convergence residual tolerance in W.
	/// Parameter of the original fermenter simulation model: `residualToleranceW`.
	double residualTolerance = 1.0;

	/// Under-relaxation factor (0 < omega <= 1).
	/// Parameter of the original fermenter simulation model: `relaxation`.
	double relaxation = 0.5;

	/// Maximum iterations per timestep.
	/// Parameter of the original fermenter simulation model: `maxIterations`.
	int maxIterations = 100;

	/// Average wind speed in m/s.
	/// Parameter of the original fermenter simulation model: `windMps`.
	double windMps = 3.5;
}
