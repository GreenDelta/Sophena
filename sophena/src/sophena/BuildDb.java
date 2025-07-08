package sophena;

import java.io.File;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.zeroturnaround.zip.ZipUtil;

import sophena.db.Database;
import sophena.io.datapack.Import;

/// Creates the database that is packaged with the application. You need to
/// make sure that the data package was created with the `sophdat` tool first.
/// It will replace the `databases.zip` file in the `resources` folder.
public class BuildDb {

	/// Only for development: set this to true if the packaged database should
	/// contain the product data.
	private static final boolean WITH_PRODUCTS = true;

	/// The folder where the database packages can be found.
	private static final String PACKAGE_DIR = "../sophdat/gen";
	private static final String BASE_PACK = "base_data.sophena";
	private static final String PRODUCT_PACK = "product_data.sophena";

	public static void main(String[] args) {
		try {

			// create empty database in temp. folder
			var tmpDir = Files.createTempDirectory("__sophena_db__");
			var dbDir = new File(tmpDir.toFile(), "database");
			System.out.println("generate database in: " + dbDir);
			var db = new Database(dbDir);

			// import data package(s)
			importPack(BASE_PACK, db);
			if (WITH_PRODUCTS) {
				importPack(PRODUCT_PACK, db);
			}
			System.out.println("close database");
			db.close();

			// replace the data package in the APP
			var zip = new File("./resources/database.zip");
			if (zip.exists()) {
				System.out.println("delete old database: " + zip);
				FileUtils.forceDelete(zip);
			}
			System.out.println("package new database");
			ZipUtil.pack(tmpDir.toFile(), zip);

			// cleanup
			System.out.println("delete temporary folder");
			FileUtils.deleteDirectory(tmpDir.toFile());

			System.out.println("all done!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void importPack(String pack, Database db) {
		var packDir = new File(PACKAGE_DIR);
		var packFile = new File(packDir, pack);
		if (!packFile.exists()) {
			System.out.println("data package does not exists:" + packFile);
			return;
		}
		System.out.println("import data from: " + packFile);
		new Import(packFile, db).run();
		System.out.println("  .. done");
	}

}
