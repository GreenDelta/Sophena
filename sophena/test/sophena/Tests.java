package sophena;

import sophena.db.Database;

import java.io.File;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class Tests {

	private static Database db;

	public static Database getDb() {
		if (db != null)
			return db;
		String tempPath = System.getProperty("java.io.tmpdir");
		File tempDir = new File(tempPath);
		File dbDir = new File(tempDir, "sophena-test-db");
		System.out.println("using db-dir: " + dbDir);
		db = new Database(dbDir);
		return db;
	}

	public static List<Class<?>> getSubTypes(
			Class<?> superType, String packageName) throws Exception {
		var types = new ArrayList<Class<?>>();
		var urls = Tests.class.getClassLoader()
				.getResources(packageName.replace('.', '/'));
		while (urls.hasMoreElements()) {
			var url = urls.nextElement();
			var dir = new File(url.toURI());
			var files = dir.list();
			if (files == null)
				continue;
			for (var fileName : files) {
				if (!fileName.endsWith(".class"))
					continue;
				var shortName = fileName.substring(0, fileName.length() - 6);
				var fullName = packageName + "." + shortName;
				var type = Class.forName(fullName);
				if (!superType.isAssignableFrom(type)
						|| Modifier.isAbstract(type.getModifiers()))
					continue;
				types.add(type);
			}
		}
		return types;
	}
}
