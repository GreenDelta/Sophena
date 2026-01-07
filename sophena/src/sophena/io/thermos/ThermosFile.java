package sophena.io.thermos;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.openlca.commons.Res;

import sophena.db.Database;
import sophena.io.datapack.ImportGson;
import sophena.model.Consumer;

public record ThermosFile(List<Consumer> consumers) {

	public static Res<ThermosFile> readFrom(File gz, Database db) {
		if (gz == null || !gz.isFile())
			return Res.error("No valid file was provided");
		if (db == null)
			return Res.error("No database provided");

		try (var is = new GZIPInputStream(new FileInputStream(gz));
				 var reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
			var gson = ImportGson.create(db, ThermosFile.class);
			ThermosFile file = gson.fromJson(reader, ThermosFile.class);
			return Res.ok(file);
		} catch (Exception e) {
			return Res.error("Failed to read data file", e);
		}
	}
}
