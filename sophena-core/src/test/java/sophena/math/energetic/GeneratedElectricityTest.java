package sophena.math.energetic;

import org.junit.Assert;
import org.junit.Test;

import sophena.calc.ProjectResult;
import sophena.model.Boiler;
import sophena.model.Consumer;
import sophena.model.LoadProfile;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.Stats;

public class GeneratedElectricityTest {

	@Test
	public void testProducer() {
		Project project = new Project();

		Producer p = new Producer();
		p.id = "p1";
		p.boiler = new Boiler();
		p.boiler.isCoGenPlant = true;
		p.boiler.maxPower = 250;
		p.boiler.maxPowerElectric = 200;
		project.producers.add(p);

		Consumer c = new Consumer();
		c.profile = new LoadProfile();
		c.profile.dynamicData = new double[Stats.HOURS];
		c.profile.staticData = new double[Stats.HOURS];
		for (int i = 0; i < 2000; i++) {
			c.profile.dynamicData[i] = 125;
		}
		project.consumers.add(c);

		ProjectResult r = ProjectResult.calculate(project);

		double e = GeneratedElectricity.get(p, r);
		Assert.assertEquals(200_000, e, 1e-16);
	}
}
