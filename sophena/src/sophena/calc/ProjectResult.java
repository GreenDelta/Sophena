package sophena.calc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.calc.biogas.BiogasPlants;
import sophena.calc.biogas.BiogasResult;
import sophena.calc.biogas.runtime.BiogasRuntimeResult;
import sophena.calc.costs.CostCalculator;
import sophena.calc.costs.CostResult;
import sophena.calc.kpi.EmissionResult;
import sophena.calc.kpi.FuelUsage;
import sophena.calc.load.ConsumerLoadCurve;
import sophena.calc.load.ConsumerResult;
import sophena.calc.load.ProjectLoad;
import sophena.calc.simulation.EnergyResult;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.Stats;

public class ProjectResult {

	private static final Logger log = LoggerFactory
		.getLogger(ProjectResult.class);

	public final Project project;

	public EnergyResult energyResult;
	public FuelUsage fuelUsage;
	public EmissionResult emissionResult;
	public CostResult costResult;
	public CostResult costResultFunding;

	public final Map<Producer, BiogasResult> biogasResults = new HashMap<>();
	public final List<ConsumerResult> consumerResults = new ArrayList<>();

	private ProjectResult(Project project) {
		this.project = Objects.requireNonNull(project);
	}

	public static ProjectResult calculate(Project project) {
		var r = new ProjectResult(project);

		// the biogas plants must be calculated first, they are used
		// in the later calculations
		r.calculateBiogasResults();

		r.energyResult = EnergyResult.calculate(project);
		r.fuelUsage = FuelUsage.calculate(r);
		var costCalc = new CostCalculator(r);
		costCalc.withFunding(false);
		r.costResult = costCalc.calculate();
		costCalc.withFunding(true);
		r.costResultFunding = costCalc.calculate();
		consumers(project, r);

		// needs to be done after calculating the consumer results
		r.emissionResult = EmissionResult.calculate(r);
		return r;
	}

	private void calculateBiogasResults() {
		for (var p : project.producers) {
			if (p.disabled || p.biogasPlant == null)
				continue;

			var res = BiogasResult.calculate(p.biogasPlant);
			if (res.isError()) {
				var error = res.error();
				log.error("Biogas plant of producer '{}' cannot be"
					+ " calculated: {}", p.name, error);
				// clear the producer profile on error so that an old
				// result is not taken into account
				BiogasPlants.applyResult(project, p,
					BiogasRuntimeResult.emptyOf(p.biogasPlant));
				continue;
			}

			var result = res.value();
			biogasResults.put(p, result);
			BiogasPlants.applyResult(project, p, result.runtime());
		}
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
