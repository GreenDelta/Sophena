package sophena.calc;

import java.util.ArrayList;
import java.util.List;

import sophena.model.Consumer;
import sophena.model.LoadProfile;
import sophena.model.Project;
import sophena.model.Stats;

public class ProjectResult {

	public final Project project;
	public final CalcLog calcLog;

	public EnergyResult energyResult;
	public FuelUsage fuelUsage;
	public CostResult costResult;
	public CostResult costResultFunding;

	public final List<ConsumerResult> consumerResults = new ArrayList<>();

	ProjectResult(Project project) {
		this.project = project;
		calcLog = new CalcLog(
				"Berechnungsergebisse für \"" + project.name + "\"");
	}

	public static ProjectResult calculate(Project project) {
		ProjectResult r = new ProjectResult(project);
		if (project == null)
			return r;
		r.energyResult = EnergyCalculator.calculate(project);
		r.fuelUsage = FuelUsage.calculate(r);
		CostCalculator costCalc = new CostCalculator(r);
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
			if (proj.heatNet != null && proj.heatNet.interruption != null) {
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
