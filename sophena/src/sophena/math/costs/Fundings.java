package sophena.math.costs;

import sophena.calc.CalcLog;
import sophena.calc.CostResult;
import sophena.calc.CostResultItem;
import sophena.model.ConvertType;
import sophena.model.Project;

public class Fundings {

	public static double get(Project project, CostResult r, CalcLog log) {
		if (project == null || project.costSettings == null)
			return 0;
		if (log != null) {
			log.h3("Förderung");
			log.value("Investitionsförderung allg.",
					project.costSettings.funding, "EUR");
		}
		double total = project.costSettings.funding
				+ getForFundingPercent(project, r, log);
		if (log != null) {
			log.value("Förderung insgesamt", total, "EUR");
			log.println();
		}
		return total;
	}

	private static double getForFundingPercent(Project project, CostResult r, CalcLog log)
	{
		double total = 0;
		var fundingTypes = project.costSettings.fundingTypes;
		if(project.costSettings.fundingPercent == 0)
			return 0;
		var factor = project.costSettings.fundingPercent / 100;
		for (CostResultItem item: r.items) 
		{
			if (item.productType == null)
				continue;
			
			Integer fundingTypeValue = ConvertType.ProductTypeToFundingType(item.productType).getValue();
			if((fundingTypes & fundingTypeValue) > 0)
				total += factor * item.investmentCosts;
		}
		if (log != null) 
		{
			log.value("Förderung prozentual insg.", total, "EUR");
		}
		return total;
	}
}
