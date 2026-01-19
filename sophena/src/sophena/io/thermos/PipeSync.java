package sophena.io.thermos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.openlca.commons.Res;

import sophena.db.Database;
import sophena.math.energetic.HeatNets;
import sophena.model.HeatNet;
import sophena.model.HeatNetPipe;
import sophena.model.Pipe;
import sophena.model.PipeType;
import sophena.model.ProductCosts;
import sophena.model.Project;

class PipeSync {

	private final Database db;
	private final ThermosImportConfig config;
	private final Project project;
	private final ThermosFile file;

	PipeSync(Database db, ThermosImportConfig config) {
		this.db = db;
		this.config = config;
		this.project = config.project();
		this.file = config.thermosFile();
	}

	Res<Void> run() {
		if (file.network() == null) return Res.error("No network provided");

		try {
			var pipes = getAvailablePipes();
			var pipeConfig = PipeConfig.of(project, pipes);
			var plan = PipePlan.of(pipeConfig, file.network());
			if (plan.isError()) return plan.wrapError(
				"Failed to calculate pipe plan"
			);

			var sum = PipeSum.of(file.network(), plan.value());
			if (project.heatNet == null) {
				project.heatNet = new HeatNet();
				project.heatNet.id = UUID.randomUUID().toString();
			}

			if (config.isUpdateExisting()) {
				updateAll(sum.segments());
			} else {
				appendNew(sum.segments());
			}
			project.heatNet.length = HeatNets.getTrenchLengthOf(project.heatNet);
			return Res.ok();
		} catch (Exception e) {
			return Res.error("Failed to sync pipes", e);
		}
	}

	/// Get the available pipes from the selected product line.
	private List<Pipe> getAvailablePipes() {
		var pipes = new ArrayList<Pipe>();
		var manu = config.pipeManufacturer();
		var line = config.pipeProductLine();
		for (var p : db.getAll(Pipe.class)) {
			if (
				Objects.equals(p.manufacturer, manu) &&
				Objects.equals(p.productLine, line)
			) {
				pipes.add(p);
			}
		}
		return pipes;
	}

	private void updateAll(List<PipeSum.Seg> segments) {
		var used = new HashSet<String>();
		for (var seg : segments) {
			var match = BestMatch.of(seg.pipe(), project.heatNet);
			HeatNetPipe pipe;
			if (match == null) {
				pipe = addNew(seg);
			} else if (match.isSame) {
				pipe = match.existing;
				pipe.length = materialLengthOf(seg);
			} else {
				pipe = match.existing;
				pipe.pipe = seg.pipe();
				pipe.length = materialLengthOf(seg);
				pipe.name = seg.pipe().name;
				if (pipe.costs == null) {
					pipe.costs = new ProductCosts();
				}
				ProductCosts.copy(seg.pipe(), pipe.costs);
				pipe.pricePerMeter =
					seg.pipe().purchasePrice != null ? seg.pipe().purchasePrice : 0;
			}
			used.add(pipe.id);
		}

		project.heatNet.pipes.removeIf(p -> p.pipe == null || !used.contains(p.id));
	}

	private void appendNew(List<PipeSum.Seg> segments) {
		for (var seg : segments) {
			var match = BestMatch.of(seg.pipe(), project.heatNet);
			if (match != null && match.isSame) {
				match.existing.length += materialLengthOf(seg);
			} else {
				addNew(seg);
			}
		}
	}

	private HeatNetPipe addNew(PipeSum.Seg seg) {
		var hnp = new HeatNetPipe();
		hnp.id = UUID.randomUUID().toString();
		hnp.pipe = seg.pipe();
		hnp.length = materialLengthOf(seg);
		hnp.name = seg.pipe().name;
		hnp.costs = new ProductCosts();
		ProductCosts.copy(seg.pipe(), hnp.costs);
		hnp.pricePerMeter =
			seg.pipe().purchasePrice != null ? seg.pipe().purchasePrice : 0;
		project.heatNet.pipes.add(hnp);
		return hnp;
	}

	private double materialLengthOf(PipeSum.Seg seg) {
		if (seg == null || seg.pipe() == null) return 0;
		return seg.pipe().pipeType == PipeType.UNO
			? seg.length() * 2
			: seg.length();
	}

	private record BestMatch(HeatNetPipe existing, boolean isSame) {
		static BestMatch of(Pipe pipe, HeatNet net) {
			if (net.pipes.isEmpty()) return null;
			HeatNetPipe candidate = null;
			for (var hnp : net.pipes) {
				if (hnp.pipe == null) continue;
				if (Objects.equals(pipe, hnp.pipe)) return new BestMatch(hnp, true);
				if (candidate == null && eq(pipe, hnp.pipe)) {
					candidate = hnp;
				}
			}
			return candidate != null ? new BestMatch(candidate, false) : null;
		}

		private static boolean eq(Pipe a, Pipe b) {
			return Objects.equals(a.group, b.group)
				? Math.abs(a.innerDiameter - b.innerDiameter) < 1e-6
				: false;
		}
	}
}
