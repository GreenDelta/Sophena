package sophena.io.datapack;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import sophena.model.ModelType;

public class DataPack implements Closeable {

	public static final int VERSION = 2;

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final FileSystem zip;

	public static DataPack open(File zipFile) throws IOException {
		return new DataPack(zipFile);
	}

	public DataPack(File zipFile) throws IOException {
		String uriStr = zipFile.toURI().toASCIIString();
		URI uri = URI.create("jar:" + uriStr);
		Map<String, String> options = new HashMap<>();
		if (!zipFile.exists())
			options.put("create", "true");
		zip = FileSystems.newFileSystem(uri, options);
	}

	public void put(ModelType type, String id, String json) {
		if (type == null || id == null || json == null)
			return;
		try {
			String dirName = getPath(type);
			Path dir = zip.getPath(dirName);
			if (!Files.exists(dir)) {
				Files.createDirectory(dir);
			}
			Path path = zip.getPath(dirName + "/" + id + ".json");
			Files.writeString(path, json, StandardOpenOption.CREATE);
		} catch (Exception e) {
			log.error("failed to add {}/{}", type, id, e);
		}
	}

	public boolean contains(ModelType type, String id) {
		if (type == null || id == null)
			return false;
		String dirName = getPath(type);
		Path dir = zip.getPath(dirName);
		if (!Files.exists(dir))
			return false;
		Path path = zip.getPath(dirName + "/" + id + ".json");
		return Files.exists(path);
	}

	public void writeInfo() {
		try {
			Path path = zip.getPath("meta.json");
			String json = new Gson().toJson(PackInfo.current());
			Files.writeString(path, json, StandardOpenOption.CREATE);
		} catch (Exception e) {
			log.error("Failed to add meta.json", e);
		}
	}

	public PackInfo readInfo() {
		try {
			Path path = zip.getPath("meta.json");
			if (!Files.exists(path))
				return PackInfo.v1();
			JsonObject obj = readJson(path);
			Gson gson = new Gson();
			return gson.fromJson(obj, PackInfo.class);
		} catch (Exception e) {
			log.error("Failed to read meta.json; fall back to v1", e);
			return PackInfo.v1();
		}
	}

	public JsonObject get(ModelType type, String id) {
		if (!contains(type, id))
			return null;
		String dirName = getPath(type);
		Path dir = zip.getPath(dirName);
		if (!Files.exists(dir))
			return null;
		Path path = zip.getPath(dirName + "/" + id + ".json");
		if (!Files.exists(path))
			return null;
		try {
			return readJson(path);
		} catch (Exception e) {
			log.error("failed to read json object {} {}", type, id, e);
			return null;
		}
	}

	private JsonObject readJson(Path path) throws Exception {
		byte[] bytes = Files.readAllBytes(path);
		String json = new String(bytes, StandardCharsets.UTF_8);
		JsonElement elem = new Gson().fromJson(json, JsonElement.class);
		if (!elem.isJsonObject())
			return null;
		else
			return elem.getAsJsonObject();
	}

	public List<String> getIds(ModelType type) {
		String dirName = getPath(type);
		Path dir = zip.getPath(dirName);
		if (!Files.exists(dir))
			return Collections.emptyList();
		IdCollector collector = new IdCollector();
		try {
			Files.walkFileTree(dir, collector);
		} catch (Exception e) {
			log.error("failed to get ids for type {}", type, e);
		}
		return collector.ids;
	}

	@Override
	public void close() throws IOException {
		zip.close();
	}

	private String getPath(ModelType type) {
		if (type == null)
			return "unknown";
		return switch (type) {
			case BOILER -> "boilers";
			case BUFFER -> "buffers";
			case BUILDING_STATE -> "building_states";
			case CONSUMER -> "consumers";
			case COST_SETTINGS -> "cost_settings";
			case FLUE_GAS_CLEANING -> "flue_gas_cleaning";
			case FUEL -> "fuels";
			case HEAT_RECOVERY -> "heat_recovery";
			case LOAD_PROFILE -> "load_profiles";
			case MANUFACTURER -> "manufacturers";
			case PIPE -> "pipes";
			case PRODUCER -> "producers";
			case PRODUCT -> "products";
			case PRODUCT_GROUP -> "product_groups";
			case PROJECT -> "projects";
			case PROJECT_FOLDER -> "project_folders";
			case TRANSFER_STATION -> "transfer_stations";
			case WEATHER_STATION -> "weather_stations";
			case SOLAR_COLLECTOR -> "solar_collectors";
			case HEAT_PUMP -> "heat_pumps";
			case BIOGAS_SUBSTRATE -> "biogas_substrates";
		};
	}

	private static class IdCollector extends SimpleFileVisitor<Path> {

		private final List<String> ids = new ArrayList<>();

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			if (file == null)
				return FileVisitResult.CONTINUE;
			String fileName = file.getFileName().toString();
			String id = fileName.substring(0, fileName.length() - 5);
			ids.add(id);
			return FileVisitResult.CONTINUE;
		}
	}

}
