package sophena.io.thermos;

import static java.lang.Math.*;

class Pipes {

	private Pipes() {
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
		double power = Math.pow(ratio, 1.762743268);
		return 0.449677646267461 + (0.551234688 / (1 + power));
	}

	/// Calculates the mass flow rate required for a given heating load.
	///
	/// @param flowTemp    the flow temperature in °C
	/// @param returnTemp  the return temperature in °C
	/// @param heatingLoad the heating load in kW
	/// @return the mass flow rate in kg/s
	static double massFlowOf(
		double flowTemp, double returnTemp, double heatingLoad) {
		double deltaTemp = flowTemp - returnTemp;
		if (deltaTemp <= 0 || heatingLoad <= 0)
			return 0;
		double cp = WaterProps.heatCapacityOf((flowTemp + returnTemp) / 2);
		return 1000 * heatingLoad / (deltaTemp * cp * 3600);
	}

	/// Calculates the flow velocity of water in a pipe.
	///
	/// @param massFlow the mass flow rate in kg/s
	/// @param diameter the pipe diameter in m
	/// @param temp     the water temperature in °C
	/// @return the flow velocity in m/s
	static double flowVelocityOf(
		double massFlow, double diameter, double temp) {
		if (massFlow <= 0 || diameter <= 0)
			return 0;
		double radius = diameter / 2;
		double density = WaterProps.densityOf(temp);
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
		double velocity, double diameter, double roughness, double temp) {
		if (velocity <= 0 || diameter <= 0)
			return 0;
		double nu = WaterProps.kinematicViscosityOf(temp);
		double re = velocity * diameter / nu;
		double lambda = 0.25 / pow(log10(15 / re + roughness / (3.715 * diameter)), 2);
		double density = WaterProps.densityOf(temp);
		return lambda * density * pow(velocity, 2) / (diameter * 2);
	}

}
