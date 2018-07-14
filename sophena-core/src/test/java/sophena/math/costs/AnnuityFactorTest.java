package sophena.math.costs;

import org.junit.Assert;
import org.junit.Test;

import sophena.model.Project;

public class AnnuityFactorTest {

	@Test
	public void testGetForProject() {
		Project project = new Project();
		project.duration = 20;
		double af = Costs.annuityFactor(project, 2);
		Assert.assertEquals(0.0611567181252903, af, 1e-10);
	}

}
