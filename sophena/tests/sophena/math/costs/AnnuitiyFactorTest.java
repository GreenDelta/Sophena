package sophena.math.costs;

import org.junit.Assert;
import org.junit.Test;

import sophena.model.Project;

public class AnnuitiyFactorTest {

	@Test
	public void testGet() {
		double af = AnnuitiyFactor.get(1.02, 20);
		Assert.assertEquals(0.06115671812529034, af, 1e-10);
	}

	@Test
	public void testGetForProject() {
		Project project = new Project();
		project.projectDuration = 20;
		double af = AnnuitiyFactor.get(project, 2);
		Assert.assertEquals(0.0611567181252903, af, 1e-10);
	}

}
