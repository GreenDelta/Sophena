package sophena.math.costs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sophena.calc.ProjectResult;
import sophena.model.CostSettings;
import sophena.model.FlueGasCleaning;
import sophena.model.FlueGasCleaningEntry;
import sophena.model.HeatRecovery;
import sophena.model.Producer;
import sophena.model.ProductCosts;
import sophena.model.Project;

public class ProductCostTest {

	private Project project;

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
		FlueGasCleaningEntry entry = new FlueGasCleaningEntry();
		project.flueGasCleaningEntries.add(entry);
		FlueGasCleaning cleaning = new FlueGasCleaning();
		entry.product = cleaning;
		testNoCosts();
		entry.costs = new ProductCosts();
		checkCapitalCosts(entry.costs);
	}

	@Test
	public void testHeatRecovery() {
		Producer p = new Producer();
		project.producers.add(p);
		p.heatRecoveryCosts = new ProductCosts();
		p.heatRecovery = new HeatRecovery();
		checkCapitalCosts(p.heatRecoveryCosts);
	}

	private void checkCapitalCosts(ProductCosts costs) {
		costs.duration = project.projectDuration;
		costs.investment = 10_000;
		CostSettings settings = project.costSettings;
		double af = AnnuityFactor.get(project, settings.interestRate);
		double expected = af * 10_000;
		ProjectResult result = ProjectResult.calculate(project);
		double annualCosts = result.costResult.grossTotal.annualCosts;
		Assert.assertEquals(expected, annualCosts, 1e-5);
	}
}
