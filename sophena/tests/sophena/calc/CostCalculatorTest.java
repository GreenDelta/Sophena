package sophena.calc;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sophena.model.CostSettings;
import sophena.model.Project;

public class CostCalculatorTest {

	private CostCalculator calc;

	@Before
	public void setUp() {
		Project project = new Project();
		project.projectDuration = 20;
		CostSettings settings = new CostSettings();
		project.costSettings = settings;
		settings.interestRate = 2;
		settings.interestRateFunding = 1.5;
		settings.investmentFactor = 1.015;
		calc = new CostCalculator(project, new EnergyResult(project));
	}

	@Test
	public void testGetPresentValueFactorOperation() {
		double priceChangeFactor = 1.02;
		double f = calc.getCashValueFactor(priceChangeFactor);
		Assert.assertEquals(19.6078431372549, f, 1e-10);
	}

	@Test
	public void testGetResidualValue() {
		Assert.assertEquals(5609.13560393888,
				calc.getResidualValue(15, 10000), 1E-10);
		Assert.assertEquals(0,
				calc.getResidualValue(20, 10000), 1E-10);
		Assert.assertEquals(1345.94266621612,
				calc.getResidualValue(25, 10000), 1E-10);
	}

	@Test
	public void testGetCapitalCosts() {
		Assert.assertEquals(836.640588139219,
				calc.getCapitalCosts(15, 10000), 1E-10);
		Assert.assertEquals(529.253745002324,
				calc.getCapitalCosts(25, 10000), 1E-10);
		Assert.assertEquals(611.567181252905,
				calc.getCapitalCosts(20, 10000), 1E-10);
		Assert.assertEquals(1193.80830512335,
				calc.getCapitalCosts(10, 10000), 1E-10);
	}

}
