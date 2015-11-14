package sophena.math.costs;

import java.util.function.DoubleSupplier;

import org.junit.Assert;
import org.junit.Test;

import sophena.calc.CostResultItem;
import sophena.model.ProductCosts;
import sophena.model.Project;

public class ResidualValueTest {

	@Test
	public void testGet() {
		Project project = TestProject.create();
		CostResultItem item = new CostResultItem();
		item.costs = new ProductCosts();
		item.costs.investment = 10000;
		double interestRate = project.costSettings.interestRate;

		DoubleSupplier fn = () -> ResidualValue.get(item, project, interestRate);

		item.costs.duration = 15;
		Assert.assertEquals(5609.13560393888, fn.getAsDouble(), 1E-10);

		item.costs.duration = 20;
		Assert.assertEquals(0, fn.getAsDouble(), 1E-10);

		item.costs.duration = 25;
		Assert.assertEquals(1345.94266621612, fn.getAsDouble(), 1E-10);
	}

}
