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

	private ProjectResult(){
	}

	ProjectResult(Project project) {
		loadCurve = ProjectLoadCurve.get(project);
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

	public double[] getLoadCurve() {
		return loadCurve;
	}

	public double[] getSuppliedPower() {
		return suppliedPower;
	}

	public Producer[] getProducers() {
		return producers;
	}

	public double[][] getProducerResults() {
		return producerResults;
	}

	public double[] getSuppliedBufferHeat() {
		return suppliedBufferHeat;
	}

	public double[] getBufferCapacity() {
		return bufferCapacity;
	}

	public ProjectResult sort() {
		return ProjectResultSorter.sort(this);
	}

	public ProjectResult clone() {
		ProjectResult clone = new ProjectResult();
		clone.loadCurve = Arrays.copyOf(loadCurve, Stats.HOURS);
		clone.suppliedPower = Arrays.copyOf(suppliedPower, Stats.HOURS);
		clone.producers = Arrays.copyOf(producers, producers.length);
		clone.producerResults = new double[producers.length][];
		for(int i = 0; i < producers.length; i++){
			clone.producerResults[i] = Arrays.copyOf(producerResults[i],
					Stats.HOURS);
		}
		clone.suppliedBufferHeat = Arrays.copyOf(suppliedBufferHeat, Stats.HOURS);
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
