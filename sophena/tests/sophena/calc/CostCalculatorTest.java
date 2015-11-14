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

}
