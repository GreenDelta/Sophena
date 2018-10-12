package sophena.math.energetic;

import sophena.calc.ProjectResult;
import sophena.model.Producer;
import sophena.model.Stats;

/** All results are given in kWh. */
public class EfficiencyResult {

	public double fuelEnergy;
	public double conversionLoss;
	public double producedHeat;
	public double producedElectrictiy;
	public double bufferLoss;
	public double distributionLoss;
	public double usedHeat;
	public double totalLoss;

	private EfficiencyResult() {
	}

	public static EfficiencyResult calculate(ProjectResult result) {
		return new Calculator(result).calculate();
	}

	private static class Calculator {

		ProjectResult pr;

		Calculator(ProjectResult result) {
			pr = result;
		}

		EfficiencyResult calculate() {
			EfficiencyResult res = new EfficiencyResult();
			if (pr == null || pr.energyResult == null)
				return res;
			for (Producer p : pr.energyResult.producers) {
				double fuelDemand = pr.fuelUsage.getInKWh(p);
				res.fuelEnergy += fuelDemand;
				double ur;
				if (p.boiler == null || !p.boiler.isCoGenPlant)
					ur = UtilisationRate.get(pr.project, p, pr.energyResult);
				else
					ur = (p.boiler.efficiencyRate
							+ p.boiler.efficiencyRateElectric);
				double loss = fuelDemand * (1 - ur);
				res.conversionLoss += loss;
				res.producedElectrictiy += GeneratedElectricity.get(p, pr);
			}
			res.producedHeat = pr.energyResult.totalProducedHeat;
			res.distributionLoss = pr.energyResult.heatNetLoss;
			res.bufferLoss = Stats.sum(pr.energyResult.bufferLoss);
			res.usedHeat = res.producedHeat - res.distributionLoss
					- res.bufferLoss;
			res.totalLoss = res.conversionLoss + res.distributionLoss
					+ res.bufferLoss;
			return res;
		}
	}
}
