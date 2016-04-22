package sophena.db;

import java.lang.reflect.Modifier;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import sophena.Tests;
import sophena.db.daos.RootEntityDao;
import sophena.model.RootEntity;

public class RootEntityTest {

	@Test
	@SuppressWarnings("unchecked")
	public void testAll() throws Exception {
		for (Class<?> type : Tests.getSubTypes(RootEntity.class, "sophena.model")) {
			if (Modifier.isAbstract(type.getModifiers()))
				continue;
			test((Class<? extends RootEntity>) type);
		}
	}

	private <T extends RootEntity> void test(Class<T> clazz) throws Exception {
		T entity = clazz.newInstance();
		RootEntityDao<T> dao = new RootEntityDao<>(clazz, Tests.getDb());
		entity.id = UUID.randomUUID().toString();
		entity.name = "test";
		dao.insert(entity);
		T clone = dao.get(entity.id);
		Assert.assertEquals(entity, clone);
		dao.delete(entity);
		clone = dao.get(entity.id);
		Assert.assertNull(clone);
	}
}
