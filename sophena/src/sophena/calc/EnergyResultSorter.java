package sophena.calc;

import java.util.Arrays;

import sophena.model.Stats;
import sophena.utils.Num;

class EnergyResultSorter {

	static EnergyResult sort(EnergyResult result) {
		if (result == null)
			return null;
		Integer[] sortedIdx = new Integer[Stats.HOURS];
		for (int i = 0; i < Stats.HOURS; i++) {
			sortedIdx[i] = i;
		}
		double[] power = result.suppliedPower;
		double[] load = result.loadCurve;
		Arrays.sort(sortedIdx, (i1, i2) -> {
			double p1 = power[i1];
			double p2 = power[i2];
			if (Num.equal(p1, p2))
				return -Double.compare(load[i1], load[i2]);
			return -Double.compare(p1, p2);
		});
		return createCopy(sortedIdx, result);
	}

	private static EnergyResult createCopy(Integer[] idx, EnergyResult r) {
		EnergyResult s = r.clone();
		sort(idx, r.loadCurve, s.loadCurve);
		sort(idx, r.suppliedPower, s.suppliedPower);
		for (int i = 0; i < r.producers.length; i++) {
			sort(idx, r.producerResults[i], s.producerResults[i]);
		}
		sort(idx, r.suppliedBufferHeat, s.suppliedBufferHeat);
		sort(idx, r.bufferCapacity, s.bufferCapacity);
		return s;
	}

	private static void sort(Integer[] idx, double[] origin, double[] copy) {
		for (int i = 0; i < Stats.HOURS; i++) {
			copy[i] = origin[idx[i]];
		}
	}
}
