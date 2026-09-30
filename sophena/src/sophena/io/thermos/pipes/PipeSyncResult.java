package sophena.io.thermos.pipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import sophena.model.HeatNetPipe;

/// The result of a pipe sync operation, containing the pipe diffs. When the
/// sync is running in `replace` mode, it will contain all pipes that were
/// added; in `append` mode, it will only contain the newly added lengths of
/// the respective pipes.
public record PipeSyncResult(
	Map<String, PipeDiff> diffs, AtomicInteger fittingsCount
) {

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

	public List<HeatNetPipe> pipeDiffs() {
		var list = new ArrayList<HeatNetPipe>();
		for (var diff : diffs.values()) {
			var pipe = diff.pipe().copy();
			pipe.length = diff.length();
			list.add(pipe);
		}
		return list;
	}
}
