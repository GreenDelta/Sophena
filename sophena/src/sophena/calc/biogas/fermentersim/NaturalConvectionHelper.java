package sophena.calc.biogas.fermentersim;

/**
 * Computes natural convection parameters over internal liquid substrate surface.
 */
final class NaturalConvectionHelper {

	record NaturalConvectionResult(
		double hNatWm2K,
		double rayleigh,
		double nusselt
	) {
	}

	private NaturalConvectionHelper() {
	}

	static NaturalConvectionResult compute(
		double tSetC,
		double tsInnerMembraneC,
		double aRoofProjectedM2,
		double r1,
		MaterialConstants m,
		SimulationConstants c
	) {
		double naturalConvectionLengthM = aRoofProjectedM2 / (2.0 * Math.PI * r1);
		double tFilmK = 0.5 * (tSetC + tsInnerMembraneC) + c.k0C();

		double referenceTemperatureK = 293.15;
		double sutherlandConstantK = 110.4;

		double rhoFilmKgm3 = m.uRhoKgm3() * referenceTemperatureK / tFilmK;
		double etaFilmPas = m.uEtaPas()
			* Math.pow(tFilmK / referenceTemperatureK, 1.5)
			* (referenceTemperatureK + sutherlandConstantK)
			/ (tFilmK + sutherlandConstantK);
		double lambdaFilmWmK = m.uLambdaWmK() * Math.pow(tFilmK / referenceTemperatureK, 0.9);

		double nuFilmM2s = etaFilmPas / rhoFilmKgm3;
		double thermalDiffusivityFilmM2s = lambdaFilmWmK / (rhoFilmKgm3 * m.uCpJkgK());
		double prandtl = nuFilmM2s / thermalDiffusivityFilmM2s;

		double deltaTInnerK = Math.abs(tSetC - tsInnerMembraneC);
		double rayleigh = c.g() * (1.0 / tFilmK) * deltaTInnerK * Math.pow(naturalConvectionLengthM, 3)
			/ (nuFilmM2s * thermalDiffusivityFilmM2s);

		double nusselt;
		if (deltaTInnerK == 0.0) {
			nusselt = 0.0;
		} else if (tSetC >= tsInnerMembraneC) {
			double f2 = Math.pow(1.0 + Math.pow(0.322 / prandtl, 11.0 / 20.0), -20.0 / 11.0);
			double modifiedRayleigh = rayleigh * f2;
			if (modifiedRayleigh <= 7e4) {
				nusselt = 0.766 * Math.pow(modifiedRayleigh, 0.2);
			} else {
				nusselt = 0.15 * Math.pow(modifiedRayleigh, 1.0 / 3.0);
			}
		} else {
			double f1 = Math.pow(1.0 + Math.pow(0.492 / prandtl, 9.0 / 16.0), -16.0 / 9.0);
			nusselt = 0.6 * Math.pow(rayleigh * f1, 0.2);
		}

		double hNatWm2K = nusselt * lambdaFilmWmK / naturalConvectionLengthM;
		return new NaturalConvectionResult(hNatWm2K, rayleigh, nusselt);
	}
}
