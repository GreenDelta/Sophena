package sophena.db;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import sophena.io.datapack.Import;

/**
 * TODO: This should be located in a build-project?
 * 
 * Creates a new database.
 */
public class BuildDb {

	public static void main(String[] args) {
		try {
			Path dbDir = Files.createTempDirectory("__sophena_db__");
			Files.delete(dbDir);
			System.out.println("Database folder: " + dbDir);
			Database db = new Database(dbDir.toFile());
			Import dataImport = new Import(
					new File("../basedata/data_package.sophena"), db);
			dataImport.run();
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
