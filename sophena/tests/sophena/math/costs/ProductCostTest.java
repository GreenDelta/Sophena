package sophena.math.costs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sophena.calc.ProjectResult;
import sophena.model.CostSettings;
import sophena.model.FlueGasCleaning;
import sophena.model.FlueGasCleaningEntry;
import sophena.model.ProductCosts;
import sophena.model.Project;

public class ProductCostTest {

	private Project project;
	private FlueGasCleaningEntry entry;

	@Before
	public void setUp() {
		project = TestProject.create();

	}

	@Test
	public void testNoCosts() {
		ProjectResult result = ProjectResult.calculate(project);
		double annualCosts = result.costResult.grossTotal.annualCosts;
		Assert.assertEquals(0, annualCosts, 1e-16);
	}

	@Test
	public void testFlueGasCleaning() {
		entry = new FlueGasCleaningEntry();
		project.flueGasCleaningEntries.add(entry);
		FlueGasCleaning cleaning = new FlueGasCleaning();
		entry.product = cleaning;
		testNoCosts();
		entry.costs = new ProductCosts();
		entry.costs.duration = project.projectDuration;
		entry.costs.investment = 10_000;
		checkCapitalCosts();
	}

	private void checkCapitalCosts() {
		CostSettings settings = project.costSettings;
		double af = AnnuityFactor.get(project, settings.interestRate);
		double expected = af * 10_000;
		ProjectResult result = ProjectResult.calculate(project);
		double annualCosts = result.costResult.grossTotal.annualCosts;
		Assert.assertEquals(expected, annualCosts, 1e-5);
	}

}
