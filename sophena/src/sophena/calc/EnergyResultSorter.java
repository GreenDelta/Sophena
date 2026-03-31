package sophena.calc;

import java.util.Arrays;

import sophena.model.Stats;
import sophena.utils.Num;

class EnergyResultSorter {

	static EnergyResult sort(EnergyResult result) {
		if (result == null)
			return null;

		var hours = new Integer[Stats.HOURS];
		for (int i = 0; i < Stats.HOURS; i++) {
			hours[i] = i;
		}

		double[] power = result.suppliedPower;
		double[] load = result.loadCurve;
		Arrays.sort(hours, (i, j) -> {
			double mi = Math.max(load[i], power[i]);
			double mj = Math.max(load[j], power[j]);
			return Num.equal(mi, mj)
				? -Double.compare(load[i], load[j])
				: -Double.compare(mi, mj);
		});
		return createCopy(hours, result);
	}

	private static EnergyResult createCopy(Integer[] hours, EnergyResult r) {
		var s = r.clone();
		sort(hours, r.loadCurve, s.loadCurve);
		sort(hours, r.suppliedPower, s.suppliedPower);
		for (int i = 0; i < r.producers.length; i++) {
			sort(hours, r.producerResults[i], s.producerResults[i]);
		}
		sort(hours, r.suppliedBufferHeat, s.suppliedBufferHeat);
		sort(hours, r.bufferCapacity, s.bufferCapacity);
		return s;
	}

	private static void sort(Integer[] hours, double[] origin, double[] copy) {
		if (origin == null || copy == null) return;
		for (int i = 0; i < Stats.HOURS; i++) {
			copy[i] = origin[hours[i]];
		}
	}
}
