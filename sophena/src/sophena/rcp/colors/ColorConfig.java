package sophena.rcp.colors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.eclipse.swt.graphics.RGB;
import org.slf4j.LoggerFactory;
import sophena.io.Json;
import sophena.rcp.Workspace;
import sophena.rcp.utils.Colors;
import sophena.utils.Strings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ColorConfig {

	private static final RGB DEFAULT = new RGB(90, 90, 100);
	private static ColorConfig instance;

	private final List<Group> groups = new ArrayList<>();

	public static ColorConfig get() {
		if (instance != null)
			return instance;
		// TODO: read from default
		var file = new File(Workspace.dir(), "colors.json");
		instance = read(file);
		return instance;
	}

	public static ColorConfig read(File file) {
		if (file == null || file.exists())
			return new ColorConfig();
		try (var stream = new FileInputStream(file);
				 var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
			var json = new Gson().fromJson(reader, JsonObject.class);
			return read(json);
		} catch (Exception e) {
			var logger = LoggerFactory.getLogger(ColorConfig.class);
			logger.error("failed to read color config. from " + file, e);
			return new ColorConfig();
		}
	}

	private static ColorConfig read(JsonObject json) {
		var config = new ColorConfig();
		var groups = Json.getArray(json, "groups");
		if (groups != null) {
			for (var e : groups) {
				if (!e.isJsonObject())
					continue;
				Group.fromJson(config, e.getAsJsonObject());
			}
		}
		return config;
	}

	public void save(ColorConfig config) {
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
		var groups = new JsonArray();
		this.groups.stream()
				.map(Group::toJson)
				.forEach(groups::add);
		obj.add("groups", groups);
		return obj;
	}

	/**
	 * Returns the group with the given ID. Creates a new
	 * group with the given ID and label if it does not
	 * exist yet.
	 */
	public Group groupOf(String id, String label) {
		for (var group : groups) {
			if (Objects.equals(id, group.id))
				return group;
		}
		var g = new Group(id, label);
		groups.add(g);
		return g;
	}

	public static class Group {
		private final String id;
		private final String label;
		private RGB base;
		private final List<RGB> variants = new ArrayList<>();

		private Group(String id, String label) {
			this.id = id;
			this.label = label;
		}

		public String id() {
			return id;
		}

		public String label() {
			return label;
		}

		public RGB base() {
			return base != null ? base : DEFAULT;
		}

		public void setBase(RGB rgb) {
			this.base = rgb;
		}

		/**
		 * Returns the color variant for the given index. Returns the
		 * default color, if this variant was not defined yet.
		 */
		public RGB variant(int i) {
			if (i < 0 || i >= variants.size())
				return DEFAULT;
			var v = variants.get(i);
			return v != null ? v : DEFAULT;
		}

		public void setVariant(int i, RGB rgb) {
			if (rgb == null)
				return;
			while (variants.size() <= i) {
				variants.add(DEFAULT);
			}
			variants.set(i, rgb);
		}

		private JsonObject toJson() {
			var obj = new JsonObject();
			obj.addProperty("id", id);
			obj.addProperty("label", label);
			obj.addProperty("base", Colors.toHex(base()));
			var array = new JsonArray();
			for (var v : variants) {
				array.add(Colors.toHex(v));
			}
			obj.add("variants", array);
			return obj;
		}

		private static void fromJson(ColorConfig config, JsonObject json) {
			var id = Json.getString(json, "id");
			var label = Json.getString(json, "label");
			if (Strings.nullOrEmpty(id))
				return;
			var group = config.groupOf(id, label);
			group.setBase(rgbOf(json.get("base")));
			var variants = Json.getArray(json, "variants");
			if (variants == null)
				return;
			for (int i = 0; i < variants.size(); i++) {
				var e = variants.get(i);
				group.setVariant(i, rgbOf(e));
			}
		}

		private static RGB rgbOf(JsonElement e) {
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
}
