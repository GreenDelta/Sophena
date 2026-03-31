package sophena.rcp.editors.heatnets;

import sophena.model.BufferTank;

import java.util.List;
import java.util.Optional;

class BufferEstimationDialog {

	static Optional<BufferTank> open(List<BufferCosts> costs) {
		if (costs == null || costs.isEmpty()) {
			return Optional.empty();
		}
		// TODO implement the dialog
		return Optional.empty();
	}

}
