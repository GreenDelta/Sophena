package sophena.math.costs;

import org.junit.Assert;
import org.junit.Test;

import sophena.calc.CostResultItem;
import sophena.model.ProductCosts;
import sophena.model.Project;

public class NumberOfReplacementsTest {

	@Test
	public void testGet() {
		Project project = new Project();
		project.projectDuration = 20;
		CostResultItem item = new CostResultItem();
		item.costs = new ProductCosts();
		int[] durations = { 30, 20, 19, 11, 10, 9, 5, 4, 1 };
		int[] expected = { 0, 0, 1, 1, 1, 2, 3, 4, 19 };
		for (int i = 0; i < durations.length; i++) {
			item.costs.duration = durations[i];
			int r = NumberOfReplacements.get(item, project);
			Assert.assertEquals(expected[i], r);
		}
	}

}
