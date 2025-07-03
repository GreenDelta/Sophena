package sophena.db;

import java.lang.reflect.Modifier;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;

import sophena.Tests;
import sophena.model.RootEntity;

public class RootEntityTest {

	private final Database db = Tests.getDb();

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
		T entity = clazz.getDeclaredConstructor().newInstance();
		entity.id = UUID.randomUUID().toString();
		entity.name = "test";
		db.insert(entity);
		T clone = db.get(clazz, entity.id);
		Assert.assertEquals(entity, clone);
		db.delete(entity);
		clone = db.get(clazz, entity.id);
		Assert.assertNull(clone);
	}
}
