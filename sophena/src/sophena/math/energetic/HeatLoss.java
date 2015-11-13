package sophena.math.energetic;

import sophena.calc.EnergyResult;

public class HeatLoss {

	public static double getAbsoluteConversionLossKWh(EnergyResult result) {
		if (result == null)
			return 0;
		double fuelEnergy = FuelEnergyDemand.getTotalKWh(result);
		double heat = result.totalProducedHeat;
		return fuelEnergy - heat;
	}

	public static double getRelativeConversionLoss(EnergyResult result) {
		if (result == null)
			return 0;
		double fuelEnergy = FuelEnergyDemand.getTotalKWh(result);
		if (fuelEnergy == 0)
			return 0;
		double loss = getAbsoluteConversionLossKWh(result);
		return loss / fuelEnergy;
	}

}
