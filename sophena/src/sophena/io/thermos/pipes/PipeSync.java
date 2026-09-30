package sophena.io.thermos.pipes;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.openlca.commons.Res;

import sophena.db.Database;
import sophena.io.thermos.ThermosImportConfig;
import sophena.io.thermos.file.ThermosFile;
import sophena.math.energetic.HeatNets;
import sophena.model.HeatNet;
import sophena.model.HeatNetPipe;
import sophena.model.Pipe;
import sophena.model.PipeType;
import sophena.model.ProductCosts;
import sophena.model.Project;

public class PipeSync {

	private final Database db;
	private final ThermosImportConfig config;
	private final Project project;
	private final ThermosFile file;
	private final PipeSyncResult result;

	public PipeSync(Database db, ThermosImportConfig config) {
		this.db = db;
		this.config = config;
		this.project = config.project();
		this.file = config.thermosFile();
		this.result = new PipeSyncResult();
	}

	/// Calculates the pipe plan for the network of the import file and
	/// aggregates the results. This method does not modify the project.
	public Res<PipeSum> plan() {
		var network = file.network();
		if (network == null) {
			return Res.error("No network provided");
		}

		try {
			var pipes = config.pipesForProductLine(db.getAll(Pipe.class));
			var pipeConfig = PipeConfig.of(project, pipes);
			var res = PipePlan.of(pipeConfig, network);
			if (res.isError()) {
				return res.wrapError("Failed to calculate pipe plan");
			}
			return Res.ok(PipeSum.of(network, res.value()));
		} catch (Exception e) {
			return Res.error("Failed to calculate pipe plan", e);
		}
	}

	/// Applies the given pipe plan to the heat net of the project.
	public Res<PipeSyncResult> apply(PipeSum sum) {
		if (sum == null)
			return Res.error("No pipe plan provided");

		try {
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
			project.heatNet.powerLoss = HeatNets.heatLossCoefficientOf(project.heatNet);
			return Res.ok(result);
		} catch (Exception e) {
			return Res.error("Failed to sync pipes", e);
		}
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
				updateCosts(pipe);
				result.add(pipe);
			} else {
				pipe = match.existing;
				pipe.pipe = seg.pipe();
				pipe.length = materialLengthOf(seg);
				pipe.name = seg.pipe().name;
				updateCosts(pipe);
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
				updateCosts(match.existing);
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
		updateCosts(hnp);
		project.heatNet.pipes.add(hnp);
		result.add(hnp);
		return hnp;
	}

	private void updateCosts(HeatNetPipe hnp) {
		if (hnp == null || hnp.pipe == null) {
			return;
		}
		if (hnp.costs == null) {
			hnp.costs = new ProductCosts();
			ProductCosts.copy(hnp.pipe, hnp.costs);
		}
		if (hnp.pricePerMeter == 0 && hnp.pipe.purchasePrice != null) {
			hnp.pricePerMeter = hnp.pipe.purchasePrice;
		}
		hnp.costs.investment = hnp.pricePerMeter * hnp.length;
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
				&& Math.abs(a.innerDiameter - b.innerDiameter) < 1e-6;
		}
	}
}
