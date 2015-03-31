package sophena.calc;

import java.util.Arrays;

import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.Stats;

public class ProjectResult {

	double[] loadCurve;
	double[] suppliedPower;

	Producer[] producers;
	double[][] producerResults;

	double[] suppliedBufferHeat;
	double[] bufferCapacity;

	ProjectResult(Project project) {
		loadCurve = ProjectLoadCurve.calulate(project);
		suppliedPower = new double[Stats.HOURS];
		initProducerData(project);
		suppliedBufferHeat = new double[Stats.HOURS];
		bufferCapacity = new double[Stats.HOURS];
	}

	private void initProducerData(Project project) {
		int count = project.getProducers().size();
		producers = new Producer[count];
		producerResults = new double[count][];
		for (int i = 0; i < count; i++) {
			producers[i] = project.getProducers().get(i);
			producerResults[i] = new double[Stats.HOURS];
		}
		Arrays.sort(producers, (p1, p2)
				-> Integer.compare(p1.getRank(), p2.getRank()));
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
