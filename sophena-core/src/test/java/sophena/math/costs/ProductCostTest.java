package sophena.math.costs;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sophena.calc.ProjectResult;
import sophena.model.Boiler;
import sophena.model.BufferTank;
import sophena.model.Consumer;
import sophena.model.CostSettings;
import sophena.model.FlueGasCleaning;
import sophena.model.FlueGasCleaningEntry;
import sophena.model.HeatNet;
import sophena.model.HeatNetPipe;
import sophena.model.HeatRecovery;
import sophena.model.Pipe;
import sophena.model.Producer;
import sophena.model.ProductCosts;
import sophena.model.Project;
import sophena.model.TransferStation;

public class ProductCostTest {

	private Project project;

	@Before
	public void setUp() {
		project = TestProject.create();

	}

	@Test
	public void testNoCosts() {
		ProjectResult result = ProjectResult.calculate(project);
		double annualCosts = result.costResult.grossTotal.annualSurplus;
		Assert.assertEquals(0, annualCosts, 1e-16);
	}

	@Test
	public void testFlueGasCleaning() {
		FlueGasCleaningEntry entry = new FlueGasCleaningEntry();
		project.flueGasCleaningEntries.add(entry);
		FlueGasCleaning cleaning = new FlueGasCleaning();
		entry.product = cleaning;
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

	@Test
	public void testTransferStation() {
		Consumer p = new Consumer();
		project.consumers.add(p);
		p.transferStationCosts = new ProductCosts();
		p.transferStation = new TransferStation();
		checkCapitalCosts(p.transferStationCosts);
	}

	@Test
	public void testBoiler() {
		Producer p = new Producer();
		project.producers.add(p);
		p.costs = new ProductCosts();
		p.boiler = new Boiler();
		checkCapitalCosts(p.costs);
	}

	@Test
	public void testBuffer() {
		HeatNet net = new HeatNet();
		project.heatNet = net;
		net.bufferTank = new BufferTank();
		net.bufferTankCosts = new ProductCosts();
		checkCapitalCosts(net.bufferTankCosts);
	}

	public void testPipe() {
		HeatNetPipe pipe = new HeatNetPipe();
		project.heatNet.pipes.add(pipe);
		pipe.pipe = new Pipe();
		pipe.costs = new ProductCosts();
		checkCapitalCosts(pipe.costs);
	}

	private void checkCapitalCosts(ProductCosts costs) {
		testNoCosts();
		costs.duration = project.duration;
		costs.investment = 10_000;
		CostSettings settings = project.costSettings;
		double af = Costs.annuityFactor(project, settings.interestRate);
		double expected = -af * 10_000;
		ProjectResult result = ProjectResult.calculate(project);
		double annualSurplus = result.costResult.grossTotal.annualSurplus;
		Assert.assertEquals(expected, annualSurplus, 1e-5);
	}
}
