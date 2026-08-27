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

	// material constants

	/// Wall thermal conductivity in W/(m K).
	/// Parameter of the original fermenter simulation model: `wLambdaWmK`.
	double wLambdaWmK = 2.10;

	/// Wall insulation thermal conductivity in W/(m K).
	/// Parameter of the original fermenter simulation model: `wLambdaIWmK`.
	double wLambdaIWmK = 0.035;

	/// Roof layer thermal conductivity in W/(m K).
	/// Parameter of the original fermenter simulation model: `dLambdaWmK`.
	double dLambdaWmK = 0.20;

	/// Roof insulation thermal conductivity in W/(m K).
	/// Parameter of the original fermenter simulation model: `dLambdaIWmK`.
	double dLambdaIWmK = 0.035;

	/// Floor slab thermal conductivity in W/(m K).
	/// Parameter of the original fermenter simulation model: `boLambdaWmK`.
	double boLambdaWmK = 2.10;

	/// Floor insulation thermal conductivity in W/(m K).
	/// Parameter of the original fermenter simulation model: `boLambdaIWmK`.
	double boLambdaIWmK = 0.035;

	/// Soil thermal conductivity in W/(m K).
	/// Parameter of the original fermenter simulation model: `boLambdaGrWmK`.
	double boLambdaGrWmK = 2.0;

	/// Soil thermal diffusivity in m2/s.
	/// Parameter of the original fermenter simulation model: `soilThermalDiffusivityM2s`.
	double soilThermalDiffusivityM2s = 6.5e-7;

	/// Wall emissivity.
	/// Parameter of the original fermenter simulation model: `wEpsilon`.
	double wEpsilon = 0.90;

	/// Fixed roof emissivity.
	/// Parameter of the original fermenter simulation model: `dEpsilonDA`.
	double dEpsilonDA = 0.90;

	/// Wall solar absorptivity.
	/// Parameter of the original fermenter simulation model: `wAlphaWA`.
	double wAlphaWA = 0.60;

	/// Fixed roof solar absorptivity.
	/// Parameter of the original fermenter simulation model: `dAlphaDA`.
	double dAlphaDA = 0.60;

	/// Ground solar reflectance.
	/// Parameter of the original fermenter simulation model: `groundReflectance`.
	double groundReflectance = 0.20;

	/// Air dynamic viscosity in Pa s.
	/// Parameter of the original fermenter simulation model: `uEtaPas`.
	double uEtaPas = 17.98e-6;

	/// Air specific heat capacity in J/(kg K).
	/// Parameter of the original fermenter simulation model: `uCpJkgK`.
	double uCpJkgK = 1007.0;

	/// Air thermal conductivity in W/(m K).
	/// Parameter of the original fermenter simulation model: `uLambdaWmK`.
	double uLambdaWmK = 0.02603;

	/// Air density in kg/m3.
	/// Parameter of the original fermenter simulation model: `uRhoKgm3`.
	double uRhoKgm3 = 1.1881;

	// membrane and surface optical constants

	/// Solar absorption coefficient of the outer membrane.
	/// Parameter of the original fermenter simulation model: `membraneRoofAlpha`.
	double membraneRoofAlpha = 0.60;

	/// Substrate liquid surface emissivity.
	/// Parameter of the original fermenter simulation model: `liquidSurfaceEpsilon`.
	double liquidSurfaceEpsilon = 0.95;

	/// Inner membrane emissivity on the interior side.
	/// Parameter of the original fermenter simulation model: `innerMembraneEpsilonInterior`.
	double innerMembraneEpsilonInterior = 0.90;

	/// Inner membrane emissivity on the gap side.
	/// Parameter of the original fermenter simulation model: `innerMembraneEpsilonGap`.
	double innerMembraneEpsilonGap = 0.90;

	/// Outer membrane emissivity on the gap side.
	/// Parameter of the original fermenter simulation model: `outerMembraneEpsilonGap`.
	double outerMembraneEpsilonGap = 0.90;

	/// Outer membrane emissivity on the exterior side.
	/// Parameter of the original fermenter simulation model: `outerMembraneEpsilonExterior`.
	double outerMembraneEpsilonExterior = 0.90;
}
