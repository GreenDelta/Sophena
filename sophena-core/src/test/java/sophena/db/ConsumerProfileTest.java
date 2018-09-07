package sophena.db;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;

import sophena.Tests;
import sophena.db.daos.ConsumerDao;
import sophena.model.Consumer;
import sophena.model.LoadProfile;
import sophena.model.Stats;

public class ConsumerProfileTest {

	@Test
	public void testConsumerWithProfile() {
		LoadProfile profile = LoadProfile.initEmpty();
		profile.id = UUID.randomUUID().toString();
		for (int i = 0; i < Stats.HOURS; i++) {
			profile.dynamicData[i] = 42d / i;
			profile.staticData[i] = i / 42d;
		}
		Consumer c = new Consumer();
		c.id = UUID.randomUUID().toString();
		c.profile = profile;
		ConsumerDao dao = new ConsumerDao(Tests.getDb());
		dao.insert(c);
		c = dao.get(c.id);
		for (int i = 0; i < Stats.HOURS; i++) {
			assertEquals(42d / i, c.profile.dynamicData[i], 1e-10);
			assertEquals(i / 42d, c.profile.staticData[i], 1e-10);
		}
	}
}
