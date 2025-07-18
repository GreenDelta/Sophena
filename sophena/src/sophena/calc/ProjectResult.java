package sophena.calc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import sophena.model.Project;
import sophena.model.Stats;

public class ProjectResult {

	public final Project project;
	public final CalcLog calcLog;

	public EnergyResult energyResult;
	public FuelUsage fuelUsage;
	public CO2Result co2Result;
	public CostResult costResult;
	public CostResult costResultFunding;

	public final List<ConsumerResult> consumerResults = new ArrayList<>();

	ProjectResult(Project project) {
		this.project = Objects.requireNonNull(project);
		calcLog = new CalcLog(
				"Berechnungsergebisse für \"" + project.name + "\"");
	}

	public static ProjectResult calculate(Project project) {
		var r = new ProjectResult(project);
		r.energyResult = EnergyCalculator.calculate(project, r.calcLog);
		r.fuelUsage = FuelUsage.calculate(r);
		var costCalc = new CostCalculator(r);
		costCalc.withFunding(false);
		r.costResult = costCalc.calculate();
		costCalc.withFunding(true);
		r.costResultFunding = costCalc.calculate();
		consumers(project, r);

		// needs to be done after calculating the consumer results
		r.co2Result = CO2Result.calculate(r);
		return r;
	}

	private static void consumers(Project proj, ProjectResult r) {
		for (var consumer : proj.consumers) {
			if (consumer.disabled)
				continue;
			var profile = ConsumerLoadCurve.calculate(
					consumer, proj.weatherStation);
			double[] curve = profile.calculateTotal();
			if (proj.heatNet != null && proj.heatNet.interruption != null) {
				ProjectLoad.applyInterruption(curve, proj.heatNet);
			}
			double total = Stats.sum(curve);
			var result = new ConsumerResult();
			result.consumer = consumer;
			result.heatDemand = total;
			r.consumerResults.add(result);
		}
	}

}
