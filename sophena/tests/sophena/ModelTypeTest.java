package sophena;

import org.junit.Assert;
import org.junit.Test;

import sophena.model.ModelType;
import sophena.model.RootEntity;

public class ModelTypeTest {

	/**
	 * Each class that is a root entity should have a model type.
	 */
	@Test
	public void testRootEntityClasses() throws Exception {
		for (Class<?> type : Tests.getSubTypes(RootEntity.class, "sophena.model")) {
			System.out.println(type.getSimpleName() + ".class,");
			Assert.assertNotNull("no model type for " + type,
					ModelType.forModelClass(type));
		}
	}

}
