package sophena.calc.costs;

import sophena.calc.specs.SolarModules;

public class InvestmentCosts {
	private InvestmentCosts() {}

	public static double get(CostResultItem item)
	{
		int count = 1;

		if(item.producer != null && item.producer.solarCollector != null && item.producer.solarCollectorSpec != null)
		{
			count = SolarModules.getCount(item.producer.solarCollectorSpec.solarCollectorArea, item.producer.solarCollector.collectorArea);
		}

		return item.costs.investment * count;
	}
}
