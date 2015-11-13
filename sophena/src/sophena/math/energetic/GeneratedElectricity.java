package sophena.math.energetic;

import sophena.calc.EnergyResult;
import sophena.model.Boiler;
import sophena.model.Producer;

public class GeneratedElectricity {

	public static double get(Producer producer, double generatedHeat) {
		if (producer == null || producer.boiler == null)
			return 0;
		Boiler boiler = producer.boiler;
		if (!boiler.isCoGenPlant || boiler.maxPowerElectric == 0)
			return 0;
		double fullLoadHours = FullLoadHours.get(producer, generatedHeat);
		return fullLoadHours * boiler.maxPowerElectric;
	}

	public static double getTotal(EnergyResult result) {
		if (result == null)
			return 0;
		double total = 0;
		for (Producer p : result.producers) {
			double genHeat = result.totalHeat(p);
			total += get(p, genHeat);
		}
		return total;
	}

}
