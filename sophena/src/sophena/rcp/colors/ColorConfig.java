package sophena.rcp.colors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.swt.graphics.RGB;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import sophena.io.Json;
import sophena.model.Copyable;
import sophena.model.ProductType;
import sophena.rcp.Workspace;

public class ColorConfig implements Copyable<ColorConfig> {

	static final RGB DEFAULT = new RGB(90, 90, 100);
	private static ColorConfig instance;

	private final List<ColorGroup> groups = new ArrayList<>();
	private final Map<ColorKey, RGB> keyColors = new EnumMap<>(ColorKey.class);

	public static ColorConfig get() {
		if (instance != null)
			return instance;
		var file = new File(Workspace.dir(), "colors.json");
		if (!file.exists()) {
			var stream = ColorConfig.class.getResourceAsStream("colors.json");
			if (stream != null) {
				try (stream) {
					Files.copy(stream, file.toPath());
				} catch (Exception e) {
					LoggerFactory.getLogger(ColorConfig.class)
							.error("failed to unpack default colors", e);
				}
			}
		}
		instance = read(file);
		return instance;
	}

	public static void reset() {
		instance = null;
		var file = new File(Workspace.dir(), "colors.json");
		var stream = ColorConfig.class.getResourceAsStream("colors.json");
		if (stream == null)
			return;
		try (stream) {
			Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (Exception e) {
			LoggerFactory.getLogger(ColorConfig.class)
					.error("failed to reset colors", e);
		}
	}

	public static ColorConfig read(File file) {
		if (file == null || !file.exists())
			return new ColorConfig();
		try (var stream = new FileInputStream(file);
				 var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
			var json = new Gson().fromJson(reader, JsonObject.class);
			return read(json);
		} catch (Exception e) {
			LoggerFactory.getLogger(ColorConfig.class)
					.error("failed to read color config. from " + file, e);
			return new ColorConfig();
		}
	}

	private static ColorConfig read(JsonObject json) {
		var config = new ColorConfig();

		// read key-colors
		for (var key : ColorKey.values()) {
			var e = json.get(key.toString());
			var color = rgbOf(e);
			config.keyColors.put(key, color);
		}

		// read groups
		var groups = Json.getArray(json, "groups");
		if (groups != null) {
			for (var e : groups) {
				if (!e.isJsonObject())
					continue;
				ColorGroup.fromJson(config, e.getAsJsonObject());
			}
		}
		return config;
	}

	public static void save(ColorConfig config) {
		if (config == null)
			return;
		instance = config;
		var file = new File(Workspace.dir(), "colors.json");
		write(config, file);
	}

	public static void write(ColorConfig config, File file) {
		var json = config.toJson();
		try (var stream = new FileOutputStream(file);
				 var writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
			new GsonBuilder()
					.setPrettyPrinting()
					.create()
					.toJson(json, writer);
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(ColorConfig.class);
			log.error("failed to write color config. to: " + file, e);
		}
	}

	private JsonObject toJson() {
		var obj = new JsonObject();

		// key colors
		for (var key : ColorKey.values()) {
			obj.addProperty(key.toString(), Colors.toHex(get(key)));
		}

		// groups
		var groups = new JsonArray();
		this.groups.stream()
				.map(ColorGroup::toJson)
				.forEach(groups::add);
		obj.add("groups", groups);
		return obj;
	}

	public ColorGroup groupOf(ProductType type) {
		for (var group : groups) {
			if (group.type() == type)
				return group;
		}
		var g = new ColorGroup(type);
		groups.add(g);
		return g;
	}

	public void put(ColorKey key, RGB value) {
		keyColors.put(key, value);
	}

	public RGB get(ColorKey key) {
		return keyColors.getOrDefault(key, DEFAULT);
	}

	@Override
	public ColorConfig copy() {
		var copy = new ColorConfig();
		for (var key : ColorKey.values()) {
			copy.keyColors.put(key, get(key));
		}
		for (var g : groups) {
			copy.groups.add(g.copy());
		}
		return copy;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof ColorConfig other))
			return false;

		// compare key-colors
		for (var key : ColorKey.values()) {
			var thisColor = this.get(key);
			var otherColor = other.get(key);
			if (!Objects.equals(thisColor, otherColor))
				return false;
		}

		// compare groups
		if (groups.size() != other.groups.size())
			return false;
		for (var group : groups) {
			var otherGroup = other.groups.stream()
					.filter(g -> g.type() == group.type())
					.findAny()
					.orElse(null);
			if (!group.equals(otherGroup))
				return false;
		}
		return true;
	}

	static RGB rgbOf(JsonElement e) {
		if (e == null || !e.isJsonPrimitive())
			return DEFAULT;
		var prim = e.getAsJsonPrimitive();
		var s = prim.isString()
				? prim.getAsString()
				: null;
		return s != null && s.length() >= 6
				? Colors.rgbOf(s)
				: DEFAULT;
	}

}
