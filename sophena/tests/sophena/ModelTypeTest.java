package sophena;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

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
		Enumeration<URL> urls = getClass().getClassLoader().getResources(
				"sophena/model");
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			File dir = new File(url.toURI());
			for (String fileName : dir.list()) {
				if (!fileName.endsWith(".class"))
					continue;
				String shortName = fileName.substring(0, fileName.length() - 6);
				String fullName = "sophena.model." + shortName;
				Class<?> type = Class.forName(fullName);
				if (!RootEntity.class.isAssignableFrom(type)
						|| RootEntity.class.equals(type))
					continue;
				Assert.assertNotNull("no model type for " + type,
						ModelType.forModelClass(type));
			}
		}
	}

}
