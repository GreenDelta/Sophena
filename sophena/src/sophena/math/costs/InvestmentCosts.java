package sophena.math.costs;

import sophena.calc.CostResultItem;
import sophena.math.CalculateModules;

public class InvestmentCosts {
	private InvestmentCosts() {}
	
	public static double get(CostResultItem item)
	{
		int count = 1;
		
		if(item.producer != null && item.producer.solarCollector != null && item.producer.solarCollectorSpec != null)
		{
			count = CalculateModules.getCount(item.producer.solarCollectorSpec.solarCollectorArea, item.producer.solarCollector.collectorArea);
		}
		
		return item.costs.investment * count;
	}
}
