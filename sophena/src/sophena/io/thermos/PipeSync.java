package sophena.io.thermos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.openlca.commons.Res;

import sophena.db.Database;
import sophena.math.energetic.HeatNets;
import sophena.model.HeatNet;
import sophena.model.HeatNetPipe;
import sophena.model.Pipe;
import sophena.model.PipeType;
import sophena.model.ProductCosts;
import sophena.model.Project;

/// Tracks the added length for a pipe during the sync. For new pipes, this is
/// the full length. For modified pipes, this is the positive length delta.
record PipeDiff(HeatNetPipe pipe, double length) {}

/// The result of a pipe sync operation, containing the pipe diffs. When the
/// sync is running in `replace` mode, it will contain all pipes that were
/// added; in `append` mode, it will only contain the newly added lengths of
/// the respective pipes.
record PipeSyncResult(Map<String, PipeDiff> diffs, AtomicInteger fittingsCount) {

	PipeSyncResult() {
		this(new HashMap<>(), new AtomicInteger(0));
	}

	void add(HeatNetPipe hnp) {
		if (hnp != null) {
			add(hnp, hnp.length);
		}
	}

	void add(HeatNetPipe hnp, double length) {
		if (hnp == null || hnp.pipe == null || length <= 0) {
			return;
		}
		diffs.compute(hnp.pipe.id,
			($, old) -> old == null
			? new PipeDiff(hnp, length)
			: new PipeDiff(hnp, old.length() + length));
	}

	List<HeatNetPipe> pipeDiffs() {
		var list = new ArrayList<HeatNetPipe>();
		for (var diff : diffs.values()) {
			var pipe = diff.pipe().copy();
			pipe.length = diff.length();
			list.add(pipe);
		}
		return list;
	}
}

class PipeSync {

	private final Database db;
	private final ThermosImportConfig config;
	private final Project project;
	private final ThermosFile file;
	private final PipeSyncResult result;

	PipeSync(Database db, ThermosImportConfig config) {
		this.db = db;
		this.config = config;
		this.project = config.project();
		this.file = config.thermosFile();
		this.result = new PipeSyncResult();
	}

	Res<PipeSyncResult> run() {
		if (file.network() == null) {
			return Res.error("No network provided");
		}

		try {
			var pipes = getAvailablePipes();
			var pipeConfig = PipeConfig.of(project, pipes);
			var plan = PipePlan.of(pipeConfig, file.network());
			if (plan.isError()) {
				return plan.wrapError("Failed to calculate pipe plan");
			}

			var sum = PipeSum.of(file.network(), plan.value());
			if (project.heatNet == null) {
				project.heatNet = new HeatNet();
				project.heatNet.id = UUID.randomUUID().toString();
			}
			result.fittingsCount().set(sum.fittingsCount());

			if (config.isUpdateExisting()) {
				updateAll(sum.segments());
			} else {
				appendNew(sum.segments());
			}
			project.heatNet.length = HeatNets.getTrenchLengthOf(project.heatNet);
			return Res.ok(result);
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
				result.add(pipe);
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
				result.add(pipe);
			}
			used.add(pipe.id);
		}

		project.heatNet.pipes.removeIf(p -> p.pipe == null || !used.contains(p.id));
	}

	private void appendNew(List<PipeSum.Seg> segments) {
		for (var seg : segments) {
			var match = BestMatch.of(seg.pipe(), project.heatNet);
			if (match != null && match.isSame) {
				double len = materialLengthOf(seg);
				match.existing.length += len;
				result.add(match.existing, len);
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
		result.add(hnp);
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
