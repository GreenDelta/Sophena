package sophena.model;

import java.util.function.Consumer;

public class Costs {

	private Costs() {
	}

	/**
	 * Copies the default values of the given product group to the given product
	 * costs.
	 */
	public static void copy(ProductGroup fromGroup, ProductCosts toCosts) {
		if (toCosts == null)
			return;
		if (fromGroup == null) {
			toCosts.duration = 0;
			toCosts.maintenance = 0;
			toCosts.operation = 0;
			toCosts.repair = 0;
		} else {
			toCosts.duration = fromGroup.duration;
			toCosts.maintenance = fromGroup.maintenance;
			toCosts.operation = fromGroup.operation;
			toCosts.repair = fromGroup.repair;
		}
	}

	/**
	 * Iterates over all product costs from the given project.
	 */
	public static void each(Project project, Consumer<ProductCosts> fn) {
		if (project == null || fn == null)
			return;
		for (Producer producer : project.producers) {
			if (producer.costs != null)
				fn.accept(producer.costs);
		}
		for (ProductEntry entry : project.productEntries) {
			if (entry.costs != null)
				fn.accept(entry.costs);
		}
		heatNetCosts(project.heatNet, fn);
	}

	private static void heatNetCosts(HeatNet net, Consumer<ProductCosts> fn) {
		if (net == null)
			return;
		if (net.bufferTankCosts != null)
			fn.accept(net.bufferTankCosts);
		for (HeatNetPipe pipe : net.pipes) {
			if (pipe.costs != null)
				fn.accept(pipe.costs);
		}
	}

}
