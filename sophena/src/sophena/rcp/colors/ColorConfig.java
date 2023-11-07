package sophena.rcp.colors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.eclipse.swt.graphics.RGB;
import org.slf4j.LoggerFactory;
import sophena.io.Json;
import sophena.model.Copyable;
import sophena.model.ProductType;
import sophena.rcp.Workspace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ColorConfig implements Copyable<ColorConfig> {

	static final RGB DEFAULT = new RGB(90, 90, 100);
	private static ColorConfig instance;

	private final List<ColorGroup> groups = new ArrayList<>();
	private RGB bufferTankColor;

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
		config.bufferTankColor = rgbOf(json.get("bufferTankColor"));
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
			var logger = LoggerFactory.getLogger(ColorConfig.class);
			logger.error("failed to write color config. to: " + file, e);
		}
	}

	private JsonObject toJson() {
		var obj = new JsonObject();
		obj.addProperty("bufferTankColor", Colors.toHex(forBufferTank()));
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

	public void setForBufferTank(RGB color) {
		bufferTankColor = color;
	}

	public RGB forBufferTank() {
		return bufferTankColor != null
				? bufferTankColor
				: DEFAULT;
	}

	@Override
	public ColorConfig copy() {
		var copy = new ColorConfig();
		copy.bufferTankColor = bufferTankColor;
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
		if (!Objects.equals(bufferTankColor, other.bufferTankColor))
			return false;
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
