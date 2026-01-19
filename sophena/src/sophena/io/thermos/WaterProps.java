package sophena.io.thermos;

import static java.lang.Math.pow;

/// Temperature dependent physical properties of water.
class WaterProps {

	private WaterProps() {}

	/// Returns the kinematic viscosity of water in m²/s for the given
	/// temperature in °C.
	static double kinematicViscosityOf(double temp) {
		return (
			1e-6 *
			(3.08149743497233e-12 * pow(temp, 6) -
				1.26484138735424e-09 * pow(temp, 5) +
				2.1973452386272e-07 * pow(temp, 4) -
				2.14810204481063e-05 * pow(temp, 3) +
				0.00134385455616826 * pow(temp, 2) -
				0.0584558062539435 * temp +
				1.77559040247674)
		);
	}

	/// Returns the density of water in kg/m³ for the given temperature in °C.
	static double densityOf(double temp) {
		return (
			-6.81533330354539e-12 * pow(temp, 6) +
			3.01183649666038e-09 * pow(temp, 5) -
			5.94199907277653e-07 * pow(temp, 4) +
			7.29355971893073e-05 * pow(temp, 3) -
			0.00843870111427494 * pow(temp, 2) +
			0.0610398463519777 * temp +
			999.815785345714
		);
	}

	/// Returns the specific heat capacity of water in Wh/(kg·K) for the given
	/// temperature in °C.
	static double heatCapacityOf(double temp) {
		return (
			1.70768875394404e-13 * pow(temp, 6) -
			6.43554039971814e-11 * pow(temp, 5) +
			9.80720366795211e-09 * pow(temp, 4) -
			7.7310425337263e-07 * pow(temp, 3) +
			3.62700210552373e-05 * pow(temp, 2) -
			0.000985832263269837 * temp +
			1.17245490769407
		);
	}
}
