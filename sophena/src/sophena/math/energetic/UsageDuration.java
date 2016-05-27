package sophena.math.energetic;

import java.util.Objects;

import sophena.calc.EnergyResult;
import sophena.model.Producer;

public class UsageDuration {

	private UsageDuration() {
	}

	public static int get(EnergyResult result, Producer p) {
		if (result == null || p == null)
			return 0;
		double[] vals = null;
		for (int i = 0; i < result.producers.length; i++) {
			if (Objects.equals(result.producers[i], p)) {
				vals = result.producerResults[i];
				break;
			}
		}
		if (vals == null)
			return 0;
		int hours = 0;
		for (int i = 0; i < vals.length; i++) {
			if (vals[i] != 0)
				hours++;
		}
		return hours;
	}

}
