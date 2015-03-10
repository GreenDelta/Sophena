package sophena.io.datapack;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
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
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sophena.model.ModelType;

public class DataPack implements Closeable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private FileSystem zip;

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

	public void put(ModelType type, JsonObject object) {
		String id = getId(object);
		if (type == null || id == null)
			return;
		try {
			String json = new Gson().toJson(object);
			byte[] bytes = json.getBytes("utf-8");
			String dirName = getPath(type);
			Path dir = zip.getPath(dirName);
			if (!Files.exists(dir))
				Files.createDirectory(dir);
			Path path = zip.getPath(dirName + "/" + id + ".json");
			Files.write(path, bytes, StandardOpenOption.CREATE);
		} catch (Exception e) {
			log.error("failed to add " + type + "/" + id, e);
		}
	}

	private String getId(JsonObject obj) {
		if (obj == null)
			return null;
		JsonElement elem = obj.get("id");
		if (elem == null || !elem.isJsonPrimitive())
			return null;
		else
			return elem.getAsString();
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
			log.error("failed to read json object " + type + " " + id, e);
			return null;
		}
	}
	private JsonObject readJson(Path path) throws Exception {
		byte[] bytes = Files.readAllBytes(path);
		String json = new String(bytes, "utf-8");
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
			log.error("failed to get ids for type " + type, e);
		}
		return collector.ids;
	}
	@Override
	public void close() throws IOException {
		zip.close();
	}

	private String getPath(ModelType type) {
		if(type == null)
			return "unknown";
		switch (type) {
			case FUEL:
				return "fuels";
			default:
				return "unknown";
		}
	}

	private class IdCollector extends SimpleFileVisitor<Path> {

		private List<String> ids = new ArrayList<>();

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
			if (file == null)
				return FileVisitResult.CONTINUE;
			String fileName = file.getFileName().toString();
			String id = fileName.substring(0, fileName.length() - 5);
			ids.add(id);
			return FileVisitResult.CONTINUE;
		}
	}

}
