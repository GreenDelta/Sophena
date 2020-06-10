package sophena.db;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import sophena.Tests;
import sophena.db.daos.ProjectDao;
import sophena.model.HeatNet;
import sophena.model.Project;

public class HeatNetTest {

	@Test
	public void testDefaultsAreNull() {
		var project = new Project();
		project.id = UUID.randomUUID().toString();
		project.heatNet = new HeatNet();
		project.heatNet.id	= UUID.randomUUID().toString();

		var dao = new ProjectDao(Tests.getDb());
		dao.insert(project);
		Tests.getDb().getEntityFactory().getCache().evictAll();
		var p = dao.get(project.id);

		Assert.assertNull(p.heatNet.lowerBufferLoadTemperature);
		Assert.assertNull(p.heatNet.maxLoad);
		Assert.assertNull(p.heatNet.smoothingFactor);
	}
}
