package sophena.calc.biogas.fermentersim;

/// Material thermal properties and optical constants.
///
/// @param wLambdaWmK           Wall thermal conductivity in W/(m K).
/// Parameter of the original fermenter simulation model: `wLambdaWmK`.
/// @param wLambdaIWmK          Wall insulation thermal conductivity in W/(m K).
/// Parameter of the original fermenter simulation model: `wLambdaIWmK`.
/// @param dLambdaWmK           Roof layer thermal conductivity in W/(m K).
/// Parameter of the original fermenter simulation model: `dLambdaWmK`.
/// @param dLambdaIWmK          Roof insulation thermal conductivity in W/(m K).
/// Parameter of the original fermenter simulation model: `dLambdaIWmK`.
/// @param boLambdaWmK          Floor slab thermal conductivity in W/(m K).
/// Parameter of the original fermenter simulation model: `boLambdaWmK`.
/// @param boLambdaIWmK         Floor insulation thermal conductivity in W/(m K).
/// Parameter of the original fermenter simulation model: `boLambdaIWmK`.
/// @param boLambdaGrWmK        Soil thermal conductivity in W/(m K).
/// Parameter of the original fermenter simulation model: `boLambdaGrWmK`.
/// @param soilThermalDiffusivityM2s Soil thermal diffusivity in m2/s.
/// Parameter of the original fermenter simulation model: `soilThermalDiffusivityM2s`.
/// @param wEpsilon             Wall emissivity.
/// Parameter of the original fermenter simulation model: `wEpsilon`.
/// @param dEpsilonDA           Fixed roof emissivity.
/// Parameter of the original fermenter simulation model: `dEpsilonDA`.
/// @param wAlphaWA             Wall solar absorptivity.
/// Parameter of the original fermenter simulation model: `wAlphaWA`.
/// @param dAlphaDA             Fixed roof solar absorptivity.
/// Parameter of the original fermenter simulation model: `dAlphaDA`.
/// @param groundReflectance    Ground solar reflectance.
/// Parameter of the original fermenter simulation model: `groundReflectance`.
/// @param uEtaPas              Air dynamic viscosity in Pa s.
/// Parameter of the original fermenter simulation model: `uEtaPas`.
/// @param uCpJkgK              Air specific heat capacity in J/(kg K).
/// Parameter of the original fermenter simulation model: `uCpJkgK`.
/// @param uLambdaWmK           Air thermal conductivity in W/(m K).
/// Parameter of the original fermenter simulation model: `uLambdaWmK`.
/// @param uRhoKgm3             Air density in kg/m3.
/// Parameter of the original fermenter simulation model: `uRhoKgm3`.
/// @param sCpJkgK              Substrate specific heat capacity in J/(kg K).
/// Parameter of the original fermenter simulation model: `sCpJkgK`.
record MaterialConstants(
	double wLambdaWmK,
	double wLambdaIWmK,
	double dLambdaWmK,
	double dLambdaIWmK,
	double boLambdaWmK,
	double boLambdaIWmK,
	double boLambdaGrWmK,
	double soilThermalDiffusivityM2s,
	double wEpsilon,
	double dEpsilonDA,
	double wAlphaWA,
	double dAlphaDA,
	double groundReflectance,
	double uEtaPas,
	double uCpJkgK,
	double uLambdaWmK,
	double uRhoKgm3,
	double sCpJkgK
) {

	static MaterialConstants get() {
		return new MaterialConstants(
			2.10,      // Wall thermal conductivity [W/(m K)]
			0.035,     // Wall insulation thermal conductivity [W/(m K)]
			0.20,      // Roof layer thermal conductivity [W/(m K)]
			0.035,     // Roof insulation thermal conductivity [W/(m K)]
			2.10,      // Floor slab thermal conductivity [W/(m K)]
			0.035,     // Floor insulation thermal conductivity [W/(m K)]
			2.0,       // Soil thermal conductivity [W/(m K)]
			6.5e-7,    // Soil thermal diffusivity [m2/s]
			0.90,      // Wall emissivity [-]
			0.90,      // Fixed roof emissivity [-]
			0.60,      // Wall solar absorptivity [-]
			0.60,      // Fixed roof solar absorptivity [-]
			0.20,      // Ground solar reflectance [-]
			17.98e-6,  // Air dynamic viscosity [Pa s]
			1007.0,    // Air specific heat capacity [J/(kg K)]
			0.02603,   // Air thermal conductivity [W/(m K)]
			1.1881,    // Air density [kg/m3]
			3900.0     // Substrate specific heat capacity [J/(kg K)]
		);
	}

}
