package sophena.io.thermos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import sophena.io.thermos.NetworkTree.Junction;
import sophena.model.Pipe;

record PipeSum(List<Seg> segments, int fittingsCount) {

	record Seg(Pipe pipe, double length) {
	}

	static PipeSum of(NetworkTree tree, PipePlan plan) {
		if (tree == null || plan == null) {
			return new PipeSum(List.of(), 0);
		}

		var segs = new HashMap<String, Seg>();
		int fittingsCount = 1;
		var queue = new ArrayDeque<Junction>();
		queue.add(tree.root());

		while (!queue.isEmpty()) {
			var next = queue.poll();
			fittingsCount += next.segments().size();
			for (var s : next.segments()) {
				if (s.target() instanceof Junction j) {
					queue.add(j);
				}
				var pipe = plan.pipeOf(s);
				if (pipe == null) {
					continue;
				}

				segs.compute(pipe.id, (pipeId, old) -> old == null
					? new Seg(pipe, s.length())
					: new Seg(pipe, old.length + s.length()));
			}
		}
		return new PipeSum(new ArrayList<>(segs.values()), fittingsCount);
	}

}
