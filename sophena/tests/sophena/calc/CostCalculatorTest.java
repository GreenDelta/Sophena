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
		project.setProjectDuration(20);
		CostSettings settings = new CostSettings();
		project.setCostSettings(settings);
		settings.setInterestRate(1.02);
		settings.setInterestRateFunding(1.015);
		settings.setInvestmentFactor(1.015);
		calc = new CostCalculator(project);
	}

	@Test
	public void testGetAnnuityFactor() {
		double f = calc.getAnnuityFactor();
		Assert.assertEquals(0.0611567181252903, f, 1e-10);
	}

	@Test
	public void testGetPresentValueFactorOperation() {
		double priceChangeFactor = 1.02;
		double f = calc.getCashValueFactor(priceChangeFactor);
		Assert.assertEquals(19.6078431372549, f, 1e-10);
	}

	@Test
	public void testGetNumberOfReplacements() {
		Assert.assertEquals(0, calc.getNumberOfReplacements(30));
		Assert.assertEquals(0, calc.getNumberOfReplacements(20));
		Assert.assertEquals(1, calc.getNumberOfReplacements(19));
		Assert.assertEquals(1, calc.getNumberOfReplacements(11));
		Assert.assertEquals(1, calc.getNumberOfReplacements(10));
		Assert.assertEquals(2, calc.getNumberOfReplacements(9));
		Assert.assertEquals(3, calc.getNumberOfReplacements(5));
		Assert.assertEquals(4, calc.getNumberOfReplacements(4));
		Assert.assertEquals(19, calc.getNumberOfReplacements(1));
	}

	@Test
	public void testGetCashValueOfReplacement() {
		Assert.assertEquals(9289.408414,
				calc.getCashValueOfReplacement(1, 15, 10000), 1E-5);
		Assert.assertEquals(8629.310869,
				calc.getCashValueOfReplacement(2, 15, 10000), 1E-5);
		Assert.assertEquals(8016.119299,
				calc.getCashValueOfReplacement(3, 15, 10000), 1E-5);
		Assert.assertEquals(7446.500607,
				calc.getCashValueOfReplacement(4, 15, 10000), 1E-5);
		Assert.assertEquals(6917.358539,
				calc.getCashValueOfReplacement(5, 15, 10000), 1E-5);
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
