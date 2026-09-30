package sophena.io.thermos.pipes;

import static java.lang.Math.*;

/// Some helper methods for the hydraulics calculations.
class Hydraulics {

	private Hydraulics() {
	}

	/// Calculates the diversity factor for n consumers:
	///
	/// ```
  /// f(n) = 0.449677646267461 + (0.551234688 / (1 + (n / 53.84382392) ^ 1.762743268))
  /// ```
	static double diversityFactorOf(int n) {
		if (n <= 1)
			return 1.0;
		double ratio = n / 53.84382392;
		double power = pow(ratio, 1.762743268);
		return 0.449677646267461 + (0.551234688 / (1 + power));
	}

	/// Calculates the mass flow rate required for a given heating load.
	///
	/// @param flowTemp    the flow temperature in °C
	/// @param returnTemp  the return temperature in °C
	/// @param heatingLoad the heating load in kW
	/// @return the mass flow rate in kg/s
	static double massFlowOf(
		double flowTemp, double returnTemp, double heatingLoad
	) {
		double deltaTemp = flowTemp - returnTemp;
		if (deltaTemp <= 0 || heatingLoad <= 0)
			return 0;
		double cp = heatCapacityOf((flowTemp + returnTemp) / 2);
		return (1000 * heatingLoad) / (deltaTemp * cp * 3600);
	}

	/// Calculates the flow velocity of water in a pipe.
	///
	/// @param massFlow the mass flow rate in kg/s
	/// @param diameter the pipe diameter in m
	/// @param temp     the water temperature in °C
	/// @return the flow velocity in m/s
	static double flowVelocityOf(double massFlow, double diameter, double temp) {
		if (massFlow <= 0 || diameter <= 0)
			return 0;
		double radius = diameter / 2;
		double density = densityOf(temp);
		return massFlow / (pow(radius, 2) * PI * density);
	}

	/// Calculates the pressure loss per meter of pipe.
	///
	/// @param velocity  the flow velocity in m/s
	/// @param diameter  the pipe diameter in m
	/// @param roughness the pipe roughness in m
	/// @param temp      the water temperature in °C
	/// @return the pressure loss in Pa/m
	static double pressureLossOf(
		double velocity, double diameter, double roughness, double temp
	) {
		if (velocity <= 0 || diameter <= 0)
			return 0;
		double nu = kinematicViscosityOf(temp);
		double re = (velocity * diameter) / nu;
		double lambda =
			0.25 / pow(log10(15 / re + roughness / (3.715 * diameter)), 2);
		double density = densityOf(temp);
		return (lambda * density * pow(velocity, 2)) / (diameter * 2);
	}

	// physical properties of water

	/// Returns the kinematic viscosity of water in m²/s for the given
	/// temperature in °C.
	private static double kinematicViscosityOf(double temp) {
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
	private static double densityOf(double temp) {
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
	private static double heatCapacityOf(double temp) {
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
