package sophena.calc.biogas.fermentersim;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.openlca.commons.Res;

import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.Fermenter;

/// Describes how the substrate flows through the fermenters of a biogas plant.
/// Each fermenter can have a successor (`Fermenter#successorId`) into which its
/// substrate flows; several fermenters can flow into the same successor.
/// Fermenters into which no other fermenter flows are input fermenters: they
/// share the substrate input of the plant according to their volume. We
/// assume that the full substrate mass flows from one fermenter to the next,
/// i.e. the degraded mass is not subtracted.
///
/// The substrate of a successor has the mass weighted mean target temperature
/// of its predecessors. When this temperature is higher than the target
/// temperature of the successor, we assume that the substrate enters the
/// successor with its target temperature, so that it does not need to be
/// heated (and does not cool the successor).
///
/// @param nodes the fermenters in topological order: each fermenter comes
///   after all fermenters that flow into it
record FermenterNetwork(List<Node> nodes) {

	/// A fermenter of the network.
	///
	/// @param fermenter the fermenter
	/// @param massShare the share of the substrate input of the plant that
	///   flows through the fermenter, 0..1; the shares of the input fermenters
	///   sum up to 1
	/// @param gasShare the share of the biogas that is produced in the
	///   fermenter, 0..1; this is the volume share of the fermenter on the
	///   total volume of all fermenters
	/// @param feedTemperature the temperature of the substrate that flows into
	///   the fermenter in °C, or `null` for an input fermenter that receives
	///   the substrate input of the plant
	record Node(
		Fermenter fermenter,
		double massShare,
		double gasShare,
		Double feedTemperature
	) {

		boolean isInput() {
			return feedTemperature == null;
		}
	}

	static Res<FermenterNetwork> of(BiogasPlant plant) {
		if (plant == null || plant.fermenters.isEmpty())
			return Res.error("Es wurde kein Fermenter definiert.");

		var fermenters = new ArrayList<Fermenter>();
		var ids = new HashMap<String, Fermenter>();
		for (var f : plant.fermenters) {
			if (f == null)
				continue;
			if (f.volume() <= 0)
				return Res.error("Der Fermenter " + nameOf(f)
					+ " hat kein gültiges Volumen.");
			fermenters.add(f);
			ids.put(f.id, f);
		}
		if (fermenters.isEmpty())
			return Res.error("Es wurde kein Fermenter definiert.");

		// collect the predecessors of each fermenter; references to fermenters
		// that do not exist (anymore) are ignored
		var preds = new HashMap<Fermenter, List<Fermenter>>();
		for (var f : fermenters) {
			preds.put(f, new ArrayList<>());
		}
		for (var f : fermenters) {
			var successor = successorOf(f, ids);
			if (successor != null) {
				preds.get(successor).add(f);
			}
		}

		// sort the fermenters topologically (Kahn's algorithm)
		var open = new HashMap<Fermenter, Integer>();
		var queue = new ArrayDeque<Fermenter>();
		for (var f : fermenters) {
			int n = preds.get(f).size();
			open.put(f, n);
			if (n == 0) {
				queue.add(f);
			}
		}
		var sorted = new ArrayList<Fermenter>();
		while (!queue.isEmpty()) {
			var f = queue.poll();
			sorted.add(f);
			var successor = successorOf(f, ids);
			if (successor == null)
				continue;
			int n = open.get(successor) - 1;
			open.put(successor, n);
			if (n == 0) {
				queue.add(successor);
			}
		}
		if (sorted.size() < fermenters.size())
			return Res.error("Die Fermenter sind im Kreis verschaltet.");

		// calculate the mass and gas shares and the feed temperatures
		double inputVolume = 0;
		double totalVolume = 0;
		for (var f : fermenters) {
			totalVolume += f.volume();
			if (preds.get(f).isEmpty()) {
				inputVolume += f.volume();
			}
		}

		var massShares = new HashMap<Fermenter, Double>();
		var nodes = new ArrayList<Node>(sorted.size());
		for (var f : sorted) {
			double gasShare = f.volume() / totalVolume;
			var ps = preds.get(f);
			if (ps.isEmpty()) {
				double massShare = f.volume() / inputVolume;
				massShares.put(f, massShare);
				nodes.add(new Node(f, massShare, gasShare, null));
				continue;
			}

			double massShare = 0;
			double temperature = 0;
			for (var p : ps) {
				double mi = massShares.get(p);
				massShare += mi;
				temperature += mi * p.targetTemperature;
			}
			massShares.put(f, massShare);
			double feedTemperature = massShare > 0
				? Math.min(temperature / massShare, f.targetTemperature)
				: f.targetTemperature;
			nodes.add(new Node(f, massShare, gasShare, feedTemperature));
		}

		return Res.ok(new FermenterNetwork(nodes));
	}

	private static Fermenter successorOf(
		Fermenter f, HashMap<String, Fermenter> ids
	) {
		if (f.successorId == null || Objects.equals(f.successorId, f.id))
			return null;
		return ids.get(f.successorId);
	}

	private static String nameOf(Fermenter f) {
		return f.name != null && !f.name.isBlank()
			? "\"" + f.name + "\""
			: "ohne Namen";
	}
}
