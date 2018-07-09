package sophena.math.costs;

import org.junit.Assert;
import org.junit.Test;

import sophena.math.costs.Costs;
import sophena.model.CostSettings;
import sophena.model.Project;

public class CostsTest {

	@Test
	public void testGetCashValueFactor() {
		Project project = new Project();
		project.duration = 20;
		CostSettings settings = new CostSettings();
		project.costSettings = settings;
		settings.interestRate = 2;
		settings.interestRateFunding = 1.5;
		settings.investmentFactor = 1.015;

		double priceChangeFactor = 1.02;
		double b = Costs.cashValueFactor(project, 2, priceChangeFactor);
		Assert.assertEquals(19.6078431372549, b, 1e-10);
	}

}
