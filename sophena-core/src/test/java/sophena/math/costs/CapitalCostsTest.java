package sophena.math.costs;

import java.util.function.DoubleSupplier;

import org.junit.Assert;
import org.junit.Test;

import sophena.calc.CostResultItem;
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

}
