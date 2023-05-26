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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ColorConfig implements Copyable<ColorConfig> {

	private static final RGB DEFAULT = new RGB(90, 90, 100);
	private static ColorConfig instance;

	private final List<ColorGroup> groups = new ArrayList<>();
	private RGB bufferTankColor;

	public static ColorConfig get() {
		if (instance != null)
			return instance;
		// TODO: read from default
		var file = new File(Workspace.dir(), "colors.json");
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
			var logger = LoggerFactory.getLogger(ColorConfig.class);
			logger.error("failed to read color config. from " + file, e);
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
			if (group.type == type)
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
					.filter(g -> g.type == group.type)
					.findAny()
					.orElse(null);
			if (!group.equals(otherGroup))
				return false;
		}
		return true;
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

	public static class ColorGroup implements Copyable<ColorGroup> {
		private final ProductType type;
		private RGB base;
		private final List<RGB> variants = new ArrayList<>();

		private ColorGroup(ProductType type) {
			this.type = Objects.requireNonNull(type);
		}

		public ProductType type() {
			return type;
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

		@Override
		public ColorGroup copy() {
			Function<RGB, RGB> copyRgb = rgb ->
					rgb != null
							? new RGB(rgb.red, rgb.green, rgb.blue)
							: DEFAULT;
			var copy = new ColorGroup(type);
			copy.base = copyRgb.apply(base);
			for (var v : variants) {
				copy.variants.add(copyRgb.apply(v));
			}
			return copy;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			if (!(obj instanceof ColorGroup other))
				return false;
			if (type != other.type)
				return false;
			if (variants.size() != other.variants.size())
				return false;
			for (int i = 0; i < variants.size(); i++) {
				if(!Objects.equals(variants.get(i), other.variants.get(i)))
					return false;
			}
			return true;
		}

		private JsonObject toJson() {
			var obj = new JsonObject();
			obj.addProperty("type", type.name());
			obj.addProperty("base", Colors.toHex(base()));
			var array = new JsonArray();
			for (var v : variants) {
				array.add(Colors.toHex(v));
			}
			obj.add("variants", array);
			return obj;
		}

		private static void fromJson(ColorConfig config, JsonObject json) {
			var type = ProductType.of(Json.getString(json, "type"))
					.orElse(null);
			if (type == null)
				return;
			var group = config.groupOf(type);
			group.setBase(rgbOf(json.get("base")));
			var variants = Json.getArray(json, "variants");
			if (variants == null)
				return;
			for (int i = 0; i < variants.size(); i++) {
				var e = variants.get(i);
				group.setVariant(i, rgbOf(e));
			}
		}
	}
}
