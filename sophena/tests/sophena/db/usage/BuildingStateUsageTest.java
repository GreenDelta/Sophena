package sophena.db.usage;

import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import sophena.Tests;
import sophena.db.Database;
import sophena.db.daos.BuildingStateDao;
import sophena.db.daos.ConsumerDao;
import sophena.model.BuildingState;
import sophena.model.Consumer;
import sophena.model.ModelType;

public class BuildingStateUsageTest {

	private Database db = Tests.getDb();
	private BuildingStateDao stateDao = new BuildingStateDao(db);
	private ConsumerDao consumerDao = new ConsumerDao(db);

	private BuildingState state;
	private Consumer consumer;

	@Before
	public void setUp() {
		state = new BuildingState();
		state.id = UUID.randomUUID().toString();
		stateDao.insert(state);
		consumer = new Consumer();
		consumer.id = UUID.randomUUID().toString();
		consumer.name = "Test consumer";
		consumerDao.insert(consumer);
	}

	@After
	public void tearDown() {
		consumerDao.delete(consumer);
		stateDao.delete(state);
	}

	@Test
	public void testNotUsed() {
		List<SearchResult> list = new UsageSearch(db).of(state);
		Assert.assertTrue(list.isEmpty());
	}

	@Test
	public void testUsed() {
		consumer.buildingState = state;
		consumer = consumerDao.update(consumer);
		List<SearchResult> list = new UsageSearch(db).of(state);
		Assert.assertEquals(1, list.size());
		SearchResult r = list.get(0);
		Assert.assertEquals(consumer.id, r.id);
		Assert.assertEquals(consumer.name, r.name);
		Assert.assertEquals(ModelType.CONSUMER, r.type);
	}

}
