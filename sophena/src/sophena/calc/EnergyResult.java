package sophena.calc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import org.openlca.commons.Copyable;

import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.Stats;

public class EnergyResult implements Copyable<EnergyResult> {

	public double[] loadCurve;
	public double totalLoad;

	public double[] suppliedPower;

	public Producer[] producers;
	public double[][] producerResults;
	public double totalProducedHeat;
	public int[] producerStagnationDays;
	public double[] producerJaz;

	public double[] suppliedBufferHeat;
	public double totalBufferedHeat;
	public double[] bufferLoss;
	public double totalBufferLoss;
	public double[] bufferCapacity;

	HashMap<String, Double> totalHeats = new HashMap<>();
	public double heatNetLoss;
	public double fermenterHeatDemand;

	public EnergyResult() {
	}

	EnergyResult(Project project) {
		loadCurve = ProjectLoad.getSmoothedCurve(project);
		suppliedPower = new double[Stats.HOURS];
		initProducerData(project);
		suppliedBufferHeat = new double[Stats.HOURS];
		bufferLoss = new double[Stats.HOURS];
		bufferCapacity = new double[Stats.HOURS];
		heatNetLoss = Stats.sum(ProjectLoad.getNetLoadCurve(project));
	}

	public double totalHeat(Producer p) {
		if (p == null || p.id == null)
			return 0;
		Double d = totalHeats.get(p.id);
		return d == null ? 0 : d;
	}

	public int numberOfStarts(Producer p) {
		if (p == null)
			return 0;
		double[] vec = null;
		for (int i = 0; i < producers.length; i++) {
			if (Objects.equals(p, producers[i])) {
				vec = producerResults[i];
				break;
			}
		}
		if (vec == null)
			return 0;
		boolean off = true;
		int starts = 0;
		for (double v : vec) {
			if (v == 0) {
				off = true;
				continue;
			}
			if (off) {
				starts++;
				off = false;
			}
		}
		return starts;
	}

	private void initProducerData(Project project) {
		List<Producer> list = new ArrayList<>();
		for (Producer p : project.producers) {
			if (p.disabled)
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
		Arrays.sort(producers, Comparator.comparingInt(p -> p.rank));

		producerStagnationDays = new int[count];
		producerJaz = new double[count];
	}

	public int stagnationDays(Producer p) {
		if (p == null)
			return 0;
		for (int i = 0; i < producers.length; i++) {
			if (Objects.equals(p, producers[i])) {
				return producerStagnationDays[i];
			}
		}
		return 0;
	}

	public double jaz(Producer p) {
		if (p == null)
			return 0;
		for (int i = 0; i < producers.length; i++) {
			if (Objects.equals(p, producers[i])) {
				return producerJaz[i];
			}
		}
		return 0;
	}

	public double maxPeakPowerOfAllProducers() {
		double max = 0;
		double producerPower;
		for (Producer p : producers) {
			if (p.disabled)
				continue;
			producerPower = maxPeakPower(p);
			if (producerPower > max)
				max = producerPower;
		}
		return max;
	}

	public double maxPeakPower(Producer p) {
		if (p == null)
			return 0;
		double max = 0;
		for (int i = 0; i < producers.length; i++) {
			if (Objects.equals(p, producers[i])) {
				for (int hour = 0; hour < Stats.HOURS; hour++) {
					double power = producerResults[i][hour];
					if (power > max)
						max = power;
				}
			}
		}
		return max;
	}

	public EnergyResult sort() {
		return EnergyResultSorter.sort(this);
	}

	@Override
	public EnergyResult copy() {
		var copy = new EnergyResult();
		copy.loadCurve = Arrays.copyOf(loadCurve, Stats.HOURS);
		copy.suppliedPower = Arrays.copyOf(suppliedPower, Stats.HOURS);
		copy.producers = Arrays.copyOf(producers, producers.length);
		copy.producerResults = new double[producers.length][];
		for (int i = 0; i < producers.length; i++) {
			copy.producerResults[i] = Arrays.copyOf(producerResults[i], Stats.HOURS);
		}
		copy.producerStagnationDays = Arrays.copyOf(
			producerStagnationDays, producers.length);
		copy.producerJaz = Arrays.copyOf(producerJaz, producers.length);
		copy.suppliedBufferHeat = Arrays.copyOf(suppliedBufferHeat, Stats.HOURS);
		copy.bufferLoss = Arrays.copyOf(bufferLoss, Stats.HOURS);
		copy.bufferCapacity = Arrays.copyOf(bufferCapacity, Stats.HOURS);
		copy.heatNetLoss = heatNetLoss;
		copy.fermenterHeatDemand = fermenterHeatDemand;
		return copy;
	}

}
