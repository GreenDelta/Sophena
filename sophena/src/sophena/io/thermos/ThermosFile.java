package sophena.io.thermos;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.GZIPInputStream;

import org.openlca.commons.Res;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import sophena.db.Database;
import sophena.io.Json;
import sophena.io.datapack.ImportGson;
import sophena.model.Consumer;

public record ThermosFile(List<Consumer> consumers, NetworkTree network) {

	public ThermosFile {
		Objects.requireNonNull(consumers);
	}

	public boolean isEmpty() {
		return consumers.isEmpty();
	}

	public static Res<ThermosFile> readFrom(File gz, Database db) {
		if (gz == null || !gz.isFile())
			return Res.error("No valid file was provided");
		if (db == null)
			return Res.error("No database provided");

		try (var is = new GZIPInputStream(new FileInputStream(gz));
				 var reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
			var gson = ImportGson.create(db, Consumer.class);
			var obj = gson.fromJson(reader, JsonObject.class);
			var consumers = readConsumers(gson, obj);
			var networkObj = Json.getObject(obj, "network");
			if (networkObj == null)
				return Res.error("No network found in file");
			var network = NetworkTree.parse(networkObj);
			return network.isError()
				? network.wrapError("Failed to parse network tree")
				: Res.ok(new ThermosFile(consumers, network.value()));
		} catch (Exception e) {
			return Res.error("Failed to read data file", e);
		}
	}

	private static List<Consumer> readConsumers(Gson gson, JsonObject obj) {
		var array = Json.getArray(obj, "consumers");
		if (array == null)
			return List.of();
		var consumers = new ArrayList<Consumer>(array.size());
		for (var e : array) {
			var consumer = gson.fromJson(e, Consumer.class);
			if (consumer != null) {
				consumers.add(consumer);
			}
		}
		return consumers;
	}
}
