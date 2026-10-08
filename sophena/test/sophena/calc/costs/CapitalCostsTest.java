package sophena.calc.costs;

import org.junit.Assert;
import org.junit.Test;

import sophena.model.CostSettings;
import sophena.model.ProductType;
import sophena.model.Project;

public class CapitalCostsTest {

	@Test
	public void testGet() {
		Project project = TestProject.create();
		double interestRate = project.costSettings.interestRate;
		double factor = project.costSettings.investmentFactor;

		Assert.assertEquals(836.640588139219,
				capitalCosts(project, 15, interestRate, factor), 1E-10);

		Assert.assertEquals(529.253745002324,
				capitalCosts(project, 25, interestRate, factor), 1E-10);

		Assert.assertEquals(611.567181252905,
				capitalCosts(project, 20, interestRate, factor), 1E-10);

		Assert.assertEquals(1193.80830512335,
				capitalCosts(project, 10, interestRate, factor), 1E-10);
	}

	@Test
	public void testWithReplacementsWithResidualValue() {
		Project project = new Project();
		project.duration = 20;
		project.costSettings = new CostSettings();
		project.costSettings.interestRate = 2;
		project.costSettings.investmentFactor = 1.03;

		double capitalCosts = capitalCosts(project, 8, 2,
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

		double capitalCosts = capitalCosts(project, 5, 2,
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

		double capitalCosts = capitalCosts(project, 30, 2,
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

		double capitalCosts = capitalCosts(project, 20, 2,
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

	private static double capitalCosts(
			Project project, int duration, double interestRate, double factor) {
		var item = new InvestmentItem(
				"item", ProductType.OTHER_EQUIPMENT, null,
				10_000, 10_000, duration, 0, 0, 0);
		return Investments.capitalCosts(
				item, project.duration, interestRate, factor);
	}

}
