package sophena.calc.costs;

import java.util.function.DoubleSupplier;

import org.junit.Assert;
import org.junit.Test;

import sophena.model.CostSettings;
import sophena.model.ProductCosts;
import sophena.model.Project;

public class CapitalCostsTest {

	@Test
	public void testGet() {
		Project project = TestProject.create();
		CostResultItem item = new CostResultItem();
		item.costs = new ProductCosts();
		item.costs.investment = 10_000;
		double interestRate = project.costSettings.interestRate;
		DoubleSupplier fn = () -> CapitalCosts.get(item, project, interestRate,
				project.costSettings.investmentFactor);

		item.costs.duration = 15;
		Assert.assertEquals(836.640588139219, fn.getAsDouble(), 1E-10);

		item.costs.duration = 25;
		Assert.assertEquals(529.253745002324, fn.getAsDouble(), 1E-10);

		item.costs.duration = 20;
		Assert.assertEquals(611.567181252905, fn.getAsDouble(), 1E-10);

		item.costs.duration = 10;
		Assert.assertEquals(1193.80830512335, fn.getAsDouble(), 1E-10);
	}

	@Test
	public void testWithReplacementsWithResidualValue() {
		Project project = new Project();
		project.duration = 20;
		project.costSettings = new CostSettings();
		project.costSettings.interestRate = 2;
		project.costSettings.investmentFactor = 1.03;

		CostResultItem item = new CostResultItem();
		item.costs = new ProductCosts();
		item.costs.investment = 10_000;
		item.costs.duration = 8;

		double capitalCosts = CapitalCosts.get(item, project, 2,
				project.costSettings.investmentFactor);
		Assert.assertEquals(1657.4431, capitalCosts, 1e-3);
	}

	@Test
	public void testWithReplacementsNoResidualValue() {
		Project project = new Project();
		project.duration = 20;
		project.costSettings = new CostSettings();
		project.costSettings.interestRate = 2;
		project.costSettings.investmentFactor = 1.03;

		CostResultItem item = new CostResultItem();
		item.costs = new ProductCosts();
		item.costs.investment = 10_000;
		item.costs.duration = 5;

		double capitalCosts = CapitalCosts.get(item, project, 2,
				project.costSettings.investmentFactor);
		Assert.assertEquals(2635.8927, capitalCosts, 1e-3);
	}

	@Test
	public void testNoReplacementsWithResidualValue() {
		Project project = new Project();
		project.duration = 20;
		project.costSettings = new CostSettings();
		project.costSettings.interestRate = 2;
		project.costSettings.investmentFactor = 1.03;

		CostResultItem item = new CostResultItem();
		item.costs = new ProductCosts();
		item.costs.investment = 10_000;
		item.costs.duration = 30;

		double capitalCosts = CapitalCosts.get(item, project, 2,
				project.costSettings.investmentFactor);
		Assert.assertEquals(474.3781, capitalCosts, 1e-3);
	}

	@Test
	public void testNoReplacementsNoResidualValue() {
		Project project = new Project();
		project.duration = 20;
		project.costSettings = new CostSettings();
		project.costSettings.interestRate = 2;
		project.costSettings.investmentFactor = 1.03;

		CostResultItem item = new CostResultItem();
		item.costs = new ProductCosts();
		item.costs.investment = 10_000;
		item.costs.duration = 20;

		double capitalCosts = CapitalCosts.get(item, project, 2,
				project.costSettings.investmentFactor);
		Assert.assertEquals(611.5671, capitalCosts, 1e-3);
	}

	@Test
	public void testInitialAndReplacementAmounts() {
		// with equal amounts both functions are identical
		double a = CapitalCosts.calculate(10_000, 8, 20, 1.02, 1.03);
		double b = CapitalCosts.calculate(
				10_000, 10_000, 8, 20, 1.02, 1.03);
		Assert.assertEquals(a, b, 1e-10);

		// a smaller initial amount (a refurbishment share) lowers the result,
		// but the replacements still use the full amount
		double c = CapitalCosts.calculate(3_000, 10_000, 8, 20, 1.02, 1.03);
		double d = CapitalCosts.calculate(3_000, 3_000, 8, 20, 1.02, 1.03);
		Assert.assertTrue(c > d);
	}

}
