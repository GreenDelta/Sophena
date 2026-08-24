package sophena.calc.biogas.fermentersim;

/**
 * Material thermal properties and optical constants.
 */
public record FermenterMaterials(
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

	public static FermenterMaterials createDefault() {
		return new FermenterMaterials(
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
