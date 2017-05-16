package sophena.calc;

import java.util.ArrayList;
import java.util.List;

import sophena.model.Consumer;
import sophena.model.LoadProfile;
import sophena.model.Project;
import sophena.model.Stats;

public class ProjectResult {

	public EnergyResult energyResult;
	public CostResult costResult;
	public CostResult costResultFunding;

	public final List<ConsumerResult> consumerResults = new ArrayList<>();

	ProjectResult() {
	}

	public static ProjectResult calculate(Project project) {
		ProjectResult r = new ProjectResult();
		if (project == null)
			return r;
		r.energyResult = EnergyCalculator.calculate(project);
		CostCalculator costCalc = new CostCalculator(project, r.energyResult);
		costCalc.withFunding(false);
		r.costResult = costCalc.calculate();
		costCalc.withFunding(true);
		r.costResultFunding = costCalc.calculate();
		consumers(project, r);
		return r;
	}

	private static void consumers(Project proj, ProjectResult r) {
		for (Consumer consumer : proj.consumers) {
			if (consumer.disabled)
				continue;
			LoadProfile profile = ConsumerLoadCurve.calculate(consumer,
					proj.weatherStation);
			double[] curve = profile.calculateTotal();
			if (proj.heatNet != null && proj.heatNet.withInterruption) {
				ProjectLoad.applyInterruption(curve, proj.heatNet);
			}
			double total = Stats.sum(curve);
			ConsumerResult result = new ConsumerResult();
			result.consumer = consumer;
			result.heatDemand = total;
			r.consumerResults.add(result);
		}
	}

}
