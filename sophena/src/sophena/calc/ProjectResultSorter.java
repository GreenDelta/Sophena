package sophena.calc;

import java.util.Arrays;

import sophena.model.Stats;

class ProjectResultSorter {

	static ProjectResult sort(ProjectResult result) {
		if (result == null)
			return null;
		double[] power = result.getSuppliedPower();
		Integer[] sortedIdx = new Integer[Stats.HOURS];
		for (int i = 0; i < Stats.HOURS; i++) {
			sortedIdx[i] = i;
		}
		Arrays.sort(sortedIdx, (i1, i2)
				-> -Double.compare(power[i1], power[i2]));
		return createCopy(sortedIdx, result);
	}

	private static ProjectResult createCopy(Integer[] idx, ProjectResult r) {
		ProjectResult s = r.clone();
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
