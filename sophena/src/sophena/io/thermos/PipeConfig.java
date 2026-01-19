package sophena.io.thermos;

import java.util.Collections;
import java.util.List;
import sophena.model.Pipe;
import sophena.model.Project;

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
	static PipeConfig of(Project project, List<Pipe> pipes) {
		var builder = new Builder(pipes);
		if (project == null) return builder.get();

		if (project.heatNet != null) {
			var hn = project.heatNet;
			builder
				.withFlowTemperature(hn.supplyTemperature)
				.withReturnTemperature(hn.returnTemperature);
		}

		if (project.costSettings != null) {
			var cs = project.costSettings;
			builder
				.withMaxPressureLoss(cs.maxPressureLoss)
				.withMaxFlowVelocity(cs.maxFlowVelocity)
				.withFittingSurcharge(cs.fittingSurcharge);

			double r = isSteel(pipes) ? cs.roughnessSteel : cs.roughnessPlastic;
			builder.withRoughness(r * 1e-3); // mm -> m
		}
		return builder.get();
	}

	/// We can determine if the pipes are made from steel from the product group.
	/// As the selected pipes should be from the same product line, we can just
	/// test the first best pipe we find.
	private static boolean isSteel(List<Pipe> pipes) {
		if (pipes == null || pipes.isEmpty()) {
			return false;
		}
		var first = pipes.getFirst();
		var g = first != null && first.group != null ? first.group.name : null;
		if (g == null) {
			return false;
		}
		var s = g.toLowerCase();
		return s.contains("stahl") || s.contains("steel");
	}

	static class Builder {

		private final List<Pipe> pipes;
		private double maxPressureLoss = 100;
		private double maxFlowVelocity = 3.0;
		private double fittingSurcharge = 0.2;
		private double flowTemperature = 80;
		private double returnTemperature = 50;
		private double roughness = 0.002e-3;
		private double groundTemperature = 10;

		private Builder(List<Pipe> pipes) {
			this.pipes = pipes != null ? pipes : Collections.emptyList();
			pipes.sort((pi, pj) ->
				Double.compare(pi.innerDiameter, pj.innerDiameter)
			);
		}

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
