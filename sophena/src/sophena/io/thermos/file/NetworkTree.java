package sophena.io.thermos.file;

import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openlca.commons.Res;

import sophena.io.Json;

/// This is the network tree model of an import file. It has nodes and segments
/// that connect these nodes. The root of the network is the supply center. The
/// other nodes are inner nodes of the tree, called _junctions_, or leafs of the
/// tree, the buildings. The root is a junction. A junction connects to other
/// target nodes via segments which have a length.
public record NetworkTree(Junction root) {

	public sealed interface Node permits Junction, Building {
		long id();
	}

	public record Segment(long id, double length, Node target) {
	}

	public record Junction(long id, List<Segment> segments) implements Node {
	}

	public record Building(
		long id,
		String consumerId,
		double heatDemand,
		double peakLoad
	) implements Node {
	}


	public static Res<NetworkTree> parse(JsonObject obj) {
		if (obj == null)
			return Res.error("Network object is empty");

		var array = Json.getArray(obj, "nodes");
		if (array == null || array.isEmpty())
			return Res.error("Network nodes array is empty");

		// index the node objects
		var nodes = new HashMap<Long, JsonObject>();
		for (var e : array) {
			if (!e.isJsonObject())
				continue;
			var nodeObj = e.getAsJsonObject();
			long id = Json.getLong(nodeObj, "node", -1);
			if (id != -1) {
				nodes.put(id, nodeObj);
			}
		}

		if (nodes.isEmpty())
			return Res.error("No valid nodes found in network array");

		long rootId = Json.getLong(obj, "root", -1);
		if (rootId == -1)
			return Res.error("No root node ID found in network object");

		try {
			var state = new ParseState(nodes);
			var root = state.parse(rootId);
			if (root.isError()) return root.wrapError("Failed to parse network tree");
			return root.value() instanceof Junction junction
				? Res.ok(new NetworkTree(junction))
				: Res.error("Root node " + rootId + " is not a junction");
		} catch (Exception e) {
			return Res.error("Failed to parse network tree: " + e.getMessage(), e);
		}
	}

	/// Recursively parses the tree. It also checks for cycles and returns an
	/// error in that case.
	private static class ParseState {

		private final Map<Long, JsonObject> nodeMap;
		private final Map<Long, Res<Node>> memo = new HashMap<>();
		private final Set<Long> visiting = new HashSet<>();

		ParseState(Map<Long, JsonObject> nodeMap) {
			this.nodeMap = nodeMap;
		}

		Res<Node> parse(long id) {
			var cached = memo.get(id);
			if (cached != null)
				return cached;

			if (visiting.contains(id))
				return Res.error("Cycle detected at node " + id);
			visiting.add(id);

			try {
				var obj = nodeMap.get(id);
				if (obj == null)
					return Res.error("Node " + id + " not found in network");

				// parse as building if the node is a consumer
				var consumerId = Json.getString(obj, "consumerId");
				if (consumerId != null) {
					Node building = new Building(
						id,
						consumerId,
						Json.getDouble(obj, "heatDemand", 0.0),
						Json.getDouble(obj, "peakLoad", 0.0)
					);
					var res = Res.ok(building);
					memo.put(id, res);
					return res;
				}

				// parse as junction otherwise
				var segments = new ArrayList<Segment>();
				var segArray = Json.getArray(obj, "segments");
				if (segArray != null) {
					for (var segElem : segArray) {
						if (!segElem.isJsonObject())
							continue;
						var segObj = segElem.getAsJsonObject();
						long segId = Json.getLong(segObj, "id", -1);
						if (segId < 0)
							continue;
						double length = Json.getDouble(segObj, "length", 0.0);
						long targetId = Json.getLong(segObj, "target", -1);
						if (targetId == -1)
							continue;
						var target = parse(targetId);
						if (target.isError())
							return target;
						segments.add(new Segment(segId, length, target.value()));
					}
				}

				Node junction = new Junction(id, segments);
				var res = Res.ok(junction);
				memo.put(id, res);
				return res;
			} finally {
				visiting.remove(id);
			}
		}
	}
}
