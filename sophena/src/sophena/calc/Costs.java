package sophena.calc;

import java.util.function.Consumer;

import sophena.model.HeatNet;
import sophena.model.HeatNetPipe;
import sophena.model.Producer;
import sophena.model.ProductCosts;
import sophena.model.ProductEntry;
import sophena.model.Project;

class Costs {

	private Costs() {
	}

	static void each(Project project, Consumer<ProductCosts> fn) {
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
