package sophena.math.energetic;

import sophena.calc.EnergyResult;
import sophena.model.Producer;

public class CO2Emissions {

	private CO2Emissions() {
	}

	public static double getElectricityCreditsKg(EnergyResult result) {
		double genElectricity = GeneratedElectricity.getTotal(result); // kWh
		double factor = 0.6148; // kg CO2 eq. / kWh
		return genElectricity * factor;
	}

	public static double getTotalWithCreditsKg(EnergyResult result) {
		return getTotalKg(result) - getElectricityCreditsKg(result);
	}

	public static double getTotalKg(EnergyResult result) {
		if (result == null)
			return 0;
		double total = 0;
		for (Producer p : result.producers) {
			double genHeat = result.totalHeat(p);
			total += getKg(p, genHeat);
		}
		return total;
	}

	public static double getKg(Producer producer, double generatedHeat) {
		double demand = FuelEnergyDemand.getKWh(producer, generatedHeat);
		double factor = getEmissionFactor(producer);
		return demand * factor / 1000;
	}

	private static double getEmissionFactor(Producer producer) {
		if (producer == null || producer.boiler == null)
			return 0;
		if (producer.boiler.fuel != null)
			return producer.boiler.fuel.co2Emissions;
		if (producer.fuelSpec != null && producer.fuelSpec.woodFuel != null)
			return producer.fuelSpec.woodFuel.co2Emissions;
		else
			return 0;
	}

}
