package sophena.calc.biogas.fermentersim;

import sophena.model.biogas.Fermenter;

/**
 * Geometric parameters and flow lengths for fermenter roof models.
 */
record RoofGeometry(
	double aRoofM2,
	double aRoofProjectedM2,
	double innerMembraneAreaM2,
	double fSkyRoof,
	double fGroundRoof,
	double roofExternalFlowLengthM,
	double channelAreaM2,
	double innerFlowLengthM,
	double outerFlowLengthM
) {

	static RoofGeometry createFixed(Fermenter f) {
		double r1 = f.wallInnerRadius();
		double aRoofProjectedM2 = Math.PI * r1 * r1;
		return new RoofGeometry(
			aRoofProjectedM2,
			aRoofProjectedM2,
			0.0,
			1.0,
			0.0,
			2.0 * r1,
			0.0,
			0.0,
			0.0
		);
	}

	static RoofGeometry createDoubleMembrane(Fermenter f) {
		double r1 = f.wallInnerRadius();
		double aRoofProjectedM2 = Math.PI * r1 * r1;
		double aRoofM2 = Math.PI * (r1 * r1 + f.roofMembraneHeight * f.roofMembraneHeight);
		double meanInnerHeight = 0.5 * f.roofMembraneHeight;
		double innerMembraneAreaM2 = Math.PI * (r1 * r1 + meanInnerHeight * meanInnerHeight);

		double fSkyRoof = 0.5 * (1.0 + aRoofProjectedM2 / aRoofM2);
		double fGroundRoof = 1.0 - fSkyRoof;

		double outerRadius = (r1 * r1 + f.roofMembraneHeight * f.roofMembraneHeight) / (2.0 * f.roofMembraneHeight);
		double outerAngle = 4.0 * Math.atan(f.roofMembraneHeight / r1);
		double outerArea = 0.5 * (outerRadius * outerRadius) * (outerAngle - Math.sin(outerAngle));
		double outerLength = outerRadius * outerAngle;

		double innerRadius = (r1 * r1 + meanInnerHeight * meanInnerHeight) / (2.0 * meanInnerHeight);
		double innerAngle = 4.0 * Math.atan(meanInnerHeight / r1);
		double innerArea = 0.5 * (innerRadius * innerRadius) * (innerAngle - Math.sin(innerAngle));
		double innerLength = innerRadius * innerAngle;

		double channelArea = outerArea - innerArea;
		if (channelArea <= 0) {
			throw new IllegalArgumentException("Double membrane support air channel cross section must be > 0.");
		}

		return new RoofGeometry(
			aRoofM2,
			aRoofProjectedM2,
			innerMembraneAreaM2,
			fSkyRoof,
			fGroundRoof,
			outerLength,
			channelArea,
			innerLength,
			outerLength
		);
	}
}
