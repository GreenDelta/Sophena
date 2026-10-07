package sophena.calc.costs;

import sophena.calc.CalcLog;
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

			var fundingType = ConvertType.ProductTypeToFundingType(item.productType);
			if (fundingType == null)
				continue;
			Integer fundingTypeValue = fundingType.getValue();
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
