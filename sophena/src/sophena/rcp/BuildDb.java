package sophena.rcp;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.zeroturnaround.zip.ZipUtil;

import sophena.db.Database;
import sophena.io.datapack.Import;

/**
 * TODO: This should be located in a build-project?
 * 
 * Creates a new database.
 */
public class BuildDb {

	private static final String PACKAGE_PATH = "../sophdat/gen/base_data.sophena";

	public static void main(String[] args) {
		try {

			Path tmpDir = Files.createTempDirectory("__sophena_db__");
			File dbDir = new File(tmpDir.toFile(), "database");
			System.out.println("Generate database in: " + dbDir);
			Database db = new Database(dbDir);

			System.out.println("Import data from: " + PACKAGE_PATH);
			Import dataImport = new Import(new File(PACKAGE_PATH), db);
			dataImport.run();
			db.close();

			File zip = new File("resources/database.zip");
			if (zip.exists()) {
				System.out.println("Delete old databse: " + zip);
				FileUtils.forceDelete(zip);
			}
			System.out.println("Package new database");
			ZipUtil.pack(tmpDir.toFile(), zip);

			System.out.println("Delete temporary folder");
			FileUtils.deleteDirectory(tmpDir.toFile());

			System.out.println("All done");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
