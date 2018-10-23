package sophena.math.energetic;

import org.junit.Assert;
import org.junit.Test;

import sophena.calc.ProjectResult;
import sophena.model.Boiler;
import sophena.model.Consumer;
import sophena.model.LoadProfile;
import sophena.model.Producer;
import sophena.model.ProducerProfile;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.model.Project;
import sophena.model.Stats;

public class GeneratedElectricityTest {

	@Test
	public void testProducer() {
		Project project = new Project();

		Producer p = new Producer();
		p.id = "p1";
		p.productGroup = new ProductGroup();
		p.productGroup.type = ProductType.COGENERATION_PLANT;
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

	@Test
	public void testProducerProfile() {
		Project project = new Project();

		Producer p = new Producer();
		project.producers.add(p);
		p.id = "p1";
		p.productGroup = new ProductGroup();
		p.productGroup.type = ProductType.COGENERATION_PLANT;
		p.profile = new ProducerProfile();
		p.profile.maxPower = new double[Stats.HOURS];
		p.profile.minPower = new double[Stats.HOURS];
		p.profileMaxPower = 250;
		p.profileMaxPowerElectric = 200;

		Consumer c = new Consumer();
		project.consumers.add(c);
		c.profile = new LoadProfile();
		c.profile.dynamicData = new double[Stats.HOURS];
		c.profile.staticData = new double[Stats.HOURS];

		for (int i = 0; i < 2000; i++) {
			p.profile.maxPower[i] = 250;
			c.profile.dynamicData[i] = 125;
		}

		ProjectResult r = ProjectResult.calculate(project);
		double e = GeneratedElectricity.get(p, r);
		Assert.assertEquals(200_000, e, 1e-16);

	}
}
