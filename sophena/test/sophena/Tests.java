package sophena;

import java.io.File;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import sophena.db.Database;

public class Tests {

	private static Database db;

	static {
		Logger log = Logger.getRootLogger();
		log.setLevel(Level.WARN);
		log.addAppender(new ConsoleAppender(new PatternLayout(
				"%-4r %-5p %c %x - %m%n")));
	}

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

	public static List<Class<?>> getSubTypes(Class<?> superType,
			String packageName) throws Exception {
		List<Class<?>> types = new ArrayList<>();
		Enumeration<URL> urls = Tests.class.getClassLoader().getResources(
				packageName.replace('.', '/'));
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			File dir = new File(url.toURI());
			for (String fileName : dir.list()) {
				if (!fileName.endsWith(".class"))
					continue;
				String shortName = fileName.substring(0, fileName.length() - 6);
				String fullName = packageName + "." + shortName;
				Class<?> type = Class.forName(fullName);
				if (!superType.isAssignableFrom(type)
						|| Modifier.isAbstract(type.getModifiers()))
					continue;
				types.add(type);
			}
		}
		return types;
	}

}
