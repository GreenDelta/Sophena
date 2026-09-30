package sophena.io.thermos.pipes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sophena.model.HeatNetPipe;

/// The result of a pipe sync operation, containing the pipe diffs. When the
/// sync is running in `replace` mode, it will contain all pipes that were
/// added; in `append` mode, it will only contain the newly added lengths of
/// the respective pipes.
public class PipeSyncResult {

	private final Map<String, PipeDiff> diffs = new HashMap<>();
	private int fittingsCount;

	PipeSyncResult() {
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
			(_, old) -> old == null
				? new PipeDiff(hnp, length)
				: new PipeDiff(hnp, old.length() + length));
	}

	void fittingsCount(int count) {
		this.fittingsCount = count;
	}

	public int fittingsCount() {
		return fittingsCount;
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

	/// Tracks the added length for a pipe during the sync. For new pipes, this is
	/// the full length. For modified pipes, this is the positive length delta.
	private record PipeDiff(HeatNetPipe pipe, double length) {
	}
}
