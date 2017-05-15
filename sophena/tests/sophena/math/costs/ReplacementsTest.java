package sophena.math.costs;

import org.junit.Assert;
import org.junit.Test;

import sophena.calc.CostResultItem;
import sophena.model.ProductCosts;
import sophena.model.Project;

public class ReplacementsTest {

	@Test
	public void testGet() {
		Project project = TestProject.create();
		CostResultItem item = new CostResultItem();
		item.costs = new ProductCosts();
		int[] durations = { 30, 20, 19, 11, 10, 9, 5, 4, 1 };
		int[] expected = { 0, 0, 1, 1, 1, 2, 3, 4, 19 };
		for (int i = 0; i < durations.length; i++) {
			item.costs.duration = durations[i];
			int r = Replacements.getNumber(item, project);
			Assert.assertEquals(expected[i], r);
		}
	}

	@Test
	public void testGetCashValueOfReplacement() {
		Project project = TestProject.create();
		double interestRate = project.costSettings.interestRate;
		CostResultItem item = new CostResultItem();
		item.costs = new ProductCosts();
		item.costs.investment = 10000;
		item.costs.duration = 15;
		Assert.assertEquals(9289.408414,
				Replacements.getPresentValue(1, item, project, interestRate), 1E-5);
		Assert.assertEquals(8629.310869,
				Replacements.getPresentValue(2, item, project, interestRate), 1E-5);
		Assert.assertEquals(8016.119299,
				Replacements.getPresentValue(3, item, project, interestRate), 1E-5);
		Assert.assertEquals(7446.500607,
				Replacements.getPresentValue(4, item, project, interestRate), 1E-5);
		Assert.assertEquals(6917.358539,
				Replacements.getPresentValue(5, item, project, interestRate), 1E-5);
	}

}
