package sophena.io.thermos;

import java.util.List;

import sophena.model.Pipe;

/// Configuration parameters for pipe dimensioning calculations.
///
/// @param maxPressureLoss maximum allowed pressure loss in Pa/m
/// @param maxFlowVelocity maximum allowed flow velocity in m/s
/// @param fittingSurcharge surcharge factor for fittings/installations (e.g., 0.2 for 20%)
/// @param flowTemperature flow (supply) temperature in °C
/// @param returnTemperature return temperature in °C
/// @param averageTemperature average of flow and return temperature in °C
/// @param roughness pipe wall roughness in m, e.g. 0.002E-3 for plastic pipes
/// @param groundTemperature ground/ambient temperature in °C for heat loss calculation
/// @param pipes the list of available pipes for selection
record PipeConfig(
	double maxPressureLoss,
	double maxFlowVelocity,
	double fittingSurcharge,
	double flowTemperature,
	double returnTemperature,
	double averageTemperature,
	double roughness,
	double groundTemperature,
	List<Pipe> pipes
) {

	static Builder forPlastic() {
		return new Builder().withRoughness(0.002e-3);
	}

	static Builder forSteel() {
		return new Builder().withRoughness(0.01e-3);
	}

	static class Builder {

		private double maxPressureLoss = 100;
		private double maxFlowVelocity = 3.0;
		private double fittingSurcharge = 0.2;
		private double flowTemperature = 80;
		private double returnTemperature = 50;
		private double roughness = 0.002e-3;
		private double groundTemperature = 10;
		private List<Pipe> pipes;

		Builder withMaxPressureLoss(double maxPressureLoss) {
			this.maxPressureLoss = maxPressureLoss;
			return this;
		}

		Builder withMaxFlowVelocity(double maxFlowVelocity) {
			this.maxFlowVelocity = maxFlowVelocity;
			return this;
		}

		Builder withFittingSurcharge(double fittingSurcharge) {
			this.fittingSurcharge = fittingSurcharge;
			return this;
		}

		Builder withFlowTemperature(double flowTemperature) {
			this.flowTemperature = flowTemperature;
			return this;
		}

		Builder withReturnTemperature(double returnTemperature) {
			this.returnTemperature = returnTemperature;
			return this;
		}

		Builder withRoughness(double roughness) {
			this.roughness = roughness;
			return this;
		}

		Builder withGroundTemperature(double groundTemperature) {
			this.groundTemperature = groundTemperature;
			return this;
		}

		Builder withPipes(List<Pipe> pipes) {
			this.pipes = pipes;
			return this;
		}

		PipeConfig get() {
			return new PipeConfig(
				maxPressureLoss,
				maxFlowVelocity,
				fittingSurcharge,
				flowTemperature,
				returnTemperature,
				(flowTemperature + returnTemperature) / 2,
				roughness,
				groundTemperature,
				pipes
			);
		}
	}
}
