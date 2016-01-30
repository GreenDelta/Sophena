package sophena.math.energetic;

import sophena.calc.ProjectResult;
import sophena.model.Producer;

/** All results are given in kWh. */
public class EfficiencyResult {

	public double fuelEnergy;
	public double conversionLoss;
	public double producedHeat;
	public double producedElectrictiy;
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
				double genHeat = pr.energyResult.totalHeat(p);
				double fuelDemand = FuelDemand.getKWh(p, genHeat);
				res.fuelEnergy += fuelDemand;
				double ur;
				if (p.boiler == null || !p.boiler.isCoGenPlant)
					ur = UtilisationRate.get(p, genHeat);
				else
					ur = (p.boiler.efficiencyRate + p.boiler.efficiencyRateElectric) / 100;
				double loss = fuelDemand * (1 - ur);
				res.conversionLoss += loss;
				res.producedElectrictiy += GeneratedElectricity.get(p, genHeat);
			}
			res.producedHeat = pr.energyResult.totalProducedHeat;
			res.distributionLoss = pr.heatNetLoss;
			res.usedHeat = res.producedHeat - res.distributionLoss;
			res.totalLoss = res.conversionLoss + res.distributionLoss;
			return res;
		}
	}
}
