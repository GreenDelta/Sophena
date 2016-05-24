package sophena.math.costs;

import java.util.function.DoubleSupplier;

import org.junit.Assert;
import org.junit.Test;

import sophena.calc.CostResultItem;
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
		DoubleSupplier fn = () -> CapitalCosts.get(item, project, interestRate);

		item.costs.duration = 15;
		Assert.assertEquals(836.640588139219, fn.getAsDouble(), 1E-10);

		item.costs.duration = 25;
		Assert.assertEquals(529.253745002324, fn.getAsDouble(), 1E-10);

		item.costs.duration = 20;
		Assert.assertEquals(611.567181252905, fn.getAsDouble(), 1E-10);

		item.costs.duration = 10;
		Assert.assertEquals(1193.80830512335, fn.getAsDouble(), 1E-10);
	}

}
