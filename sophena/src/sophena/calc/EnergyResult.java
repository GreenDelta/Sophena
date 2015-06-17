package sophena.calc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.Stats;

public class EnergyResult {

	public double[] loadCurve;
	public double[] suppliedPower;

	public Producer[] producers;
	public double[][] producerResults;

	public double[] suppliedBufferHeat;
	public double[] bufferCapacity;

	private EnergyResult() {
	}

	EnergyResult(Project project) {
		loadCurve = ProjectLoadCurve.get(project);
		suppliedPower = new double[Stats.HOURS];
		initProducerData(project);
		suppliedBufferHeat = new double[Stats.HOURS];
		bufferCapacity = new double[Stats.HOURS];
	}

	private void initProducerData(Project project) {
		List<Producer> list = new ArrayList<>();
		for (Producer p : project.getProducers()) {
			if (p.isDisabled())
				continue;
			list.add(p);
		}
		int count = list.size();
		producers = new Producer[count];
		producerResults = new double[count][];
		for (int i = 0; i < count; i++) {
			producers[i] = list.get(i);
			producerResults[i] = new double[Stats.HOURS];
		}
		Arrays.sort(producers, (p1, p2)
				-> Integer.compare(p1.getRank(), p2.getRank()));
	}

	public EnergyResult sort() {
		return EnergyResultSorter.sort(this);
	}

	public EnergyResult clone() {
		EnergyResult clone = new EnergyResult();
		clone.loadCurve = Arrays.copyOf(loadCurve, Stats.HOURS);
		clone.suppliedPower = Arrays.copyOf(suppliedPower, Stats.HOURS);
		clone.producers = Arrays.copyOf(producers, producers.length);
		clone.producerResults = new double[producers.length][];
		for (int i = 0; i < producers.length; i++) {
			clone.producerResults[i] = Arrays.copyOf(producerResults[i],
					Stats.HOURS);
		}
		clone.suppliedBufferHeat = Arrays.copyOf(suppliedBufferHeat,
				Stats.HOURS);
		clone.bufferCapacity = Arrays.copyOf(bufferCapacity, Stats.HOURS);
		return clone;
	}

	public void print() {
		printHeader();
		for (int i = 0; i < Stats.HOURS; i++) {
			printRow(i);
		}
	}

	void printHeader() {
		String header = "Hour\tLoad\tSupplied\tDifference\tBuffer Capacity\tBuffer Contribution\t";
		for (Producer p : producers) {
			header += p.getName() + "\t";
		}
		System.out.println(header);
	}

	void printRow(int i) {
		StringBuilder s = new StringBuilder();
		s.append(i + 1).append('\t')
				.append(loadCurve[i]).append('\t')
				.append(suppliedPower[i]).append('\t');
		double diff = suppliedPower[i] - loadCurve[i];
		s.append(diff).append('\t')
				.append(bufferCapacity[i]).append('\t')
				.append(suppliedBufferHeat[i]).append('\t');
		for (int k = 0; k < producers.length; k++) {
			s.append(producerResults[k][i]).append('\t');
		}
		System.out.println(s);
	}
}
