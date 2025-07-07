package sophena.db;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;

import sophena.Tests;
import sophena.model.Consumer;
import sophena.model.LoadProfile;
import sophena.model.Stats;

public class ConsumerProfileTest {

	private final Database db = Tests.getDb();

	@Test
	public void testConsumerWithProfile() {
		var profile = LoadProfile.initEmpty();
		profile.id = UUID.randomUUID().toString();
		for (int i = 0; i < Stats.HOURS; i++) {
			profile.dynamicData[i] = 42d / i;
			profile.staticData[i] = i / 42d;
		}

		var c = new Consumer();
		c.id = UUID.randomUUID().toString();
		c.profile = profile;
		db.insert(c);
		c = db.get(Consumer.class, c.id);
		for (int i = 0; i < Stats.HOURS; i++) {
			assertEquals(42d / i, c.profile.dynamicData[i], 1e-10);
			assertEquals(i / 42d, c.profile.staticData[i], 1e-10);
		}
	}
}
