package sophena.io.thermos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.openlca.commons.Res;
import sophena.io.thermos.NetworkTree.Building;
import sophena.io.thermos.NetworkTree.Junction;
import sophena.io.thermos.NetworkTree.Segment;
import sophena.model.Pipe;

class PipePlan {

	private final PipeConfig config;
	private final List<Pipe> pipes;
	private final HashMap<Long, PipeSegment> segments = new HashMap<>();
	private final HashMap<Long, PipeJunction> junctions = new HashMap<>();

	private PipePlan(PipeConfig config) {
		this.config = config;
		this.pipes = config.pipes();
	}

	static Res<PipePlan> of(PipeConfig config, NetworkTree tree) {
		if (config == null) return Res.error("No configuration provided");
		if (tree == null) return Res.error("No valid heat flow tree provided");
		try {
			var model = new PipePlan(config);
			var result = model.traverse(tree.root());
			return result.isError()
				? result.wrapError("Failed to calculate pipe-plan")
				: Res.ok(model);
		} catch (Exception e) {
			return Res.error("Failed to calculate pipe tree model", e);
		}
	}

	Pipe pipeOf(Segment segment) {
		if (segment == null) {
			return null;
		}
		var ps = segments.get(segment.id());
		return ps != null ? ps.pipe : null;
	}

	private Res<PipeJunction> traverse(Junction junction) {
		var segments = new ArrayList<PipeSegment>();
		for (var s : junction.segments()) {
			var target = s.target();

			// segment that connects a single building
			if (target instanceof Building building) {
				double load = building.peakLoad();
				var sub = new PipeJunction(building.id(), 0, load, 1, List.of());
				junctions.put(building.id(), sub);
				var segment = segmentOf(s, sub);
				if (segment.isError()) return segment.castError();
				segments.add(segment.value());
				continue;
			}

			// segment to an inner node
			if (target instanceof Junction subJunction) {
				var sub = traverse(subJunction);
				if (sub.isError()) return sub.castError();
				var segment = segmentOf(s, sub.value());
				if (segment.isError()) return segment.castError();
				segments.add(segment.value());
			}
		}
		return junctionOf(junction, segments);
	}

	private Res<PipeJunction> junctionOf(Junction j, List<PipeSegment> segments) {
		double netLoad = 0;
		int buildingCount = 0;
		double buildingLoad = 0;
		for (var s : segments) {
			netLoad += s.netLoad;
			buildingCount += s.buildingCount;
			buildingLoad += s.buildingLoad;
		}
		var junction = new PipeJunction(
			j.id(),
			netLoad,
			buildingLoad,
			buildingCount,
			segments
		);
		junctions.put(junction.id, junction);
		return Res.ok(junction);
	}

	private Res<PipeSegment> segmentOf(Segment s, PipeJunction sub) {
		// peakLoad in kW
		double peakLoad = sub.peakLoad();
		// temperature difference in K (°C difference equals K difference)
		double deltaT = config.averageTemperature() - config.groundTemperature();

		Pipe pipe = null;
		double segmentLoad = 0;
		for (var p : pipes) {
			// pipe heat loss: Q_loss = U * L * ΔT
			// U in W/(m·K), length in m, ΔT in K => pipeLoss in W
			// convert to kW by dividing by 1000
			double pipeLoss = (p.uValue * s.length() * deltaT) / 1000;

			// totalLoad in kW (peakLoad in kW + pipeLoss in kW)
			double totalLoad = peakLoad + pipeLoss;

			// inner diameter in m (converted from mm)
			double di = p.innerDiameter / 1000;
			double massFlow = Pipes.massFlowOf(
				config.flowTemperature(),
				config.returnTemperature(),
				totalLoad
			);
			double velocity = Pipes.flowVelocityOf(
				massFlow,
				di,
				config.averageTemperature()
			);
			if (velocity > config.maxFlowVelocity()) continue;
			var pressureLoss =
				Pipes.pressureLossOf(
					velocity,
					di,
					config.roughness(),
					config.averageTemperature()
				) *
				(1 + config.fittingSurchargePressure());
			if (pressureLoss < config.maxPressureLoss()) {
				pipe = p;
				segmentLoad = pipeLoss;
				break;
			}
		}

		if (pipe == null) {
			return Res.error(
				"No suitable pipe found for segment " +
					s.id() +
					" with peak load " +
					peakLoad +
					" W"
			);
		}

		var segment = new PipeSegment(
			s.id(),
			s.length(),
			segmentLoad + sub.netLoad,
			sub.buildingLoad,
			sub.buildingCount,
			pipe
		);
		segments.put(segment.id, segment);
		return Res.ok(segment);
	}

	record PipeJunction(
		long id,
		double netLoad,
		double buildingLoad,
		int buildingCount,
		List<PipeSegment> segments
	) {
		public double peakLoad() {
			return netLoad + buildingLoad * Pipes.diversityFactorOf(buildingCount);
		}
	}

	record PipeSegment(
		long id,
		double length,
		double netLoad,
		double buildingLoad,
		int buildingCount,
		Pipe pipe
	) {
		public double peakLoad() {
			return netLoad + buildingLoad * Pipes.diversityFactorOf(buildingCount);
		}
	}
}
