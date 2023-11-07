package sophena.rcp.colors;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.eclipse.swt.graphics.RGB;
import sophena.io.Json;
import sophena.model.Copyable;
import sophena.model.ProductType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ColorGroup implements Copyable<ColorGroup> {

	private final ProductType type;
	private RGB base;
	private final List<RGB> variants = new ArrayList<>();

	ColorGroup(ProductType type) {
		this.type = Objects.requireNonNull(type);
	}

	public ProductType type() {
		return type;
	}

	public RGB base() {
		return base != null ? base : ColorConfig.DEFAULT;
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
			return ColorConfig.DEFAULT;
		var v = variants.get(i);
		return v != null ? v : ColorConfig.DEFAULT;
	}

	public void setVariant(int i, RGB rgb) {
		if (rgb == null)
			return;
		while (variants.size() <= i) {
			variants.add(ColorConfig.DEFAULT);
		}
		variants.set(i, rgb);
	}

	@Override
	public ColorGroup copy() {
		Function<RGB, RGB> copyRgb = rgb ->
				rgb != null
						? new RGB(rgb.red, rgb.green, rgb.blue)
						: ColorConfig.DEFAULT;
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
			if (!Objects.equals(variants.get(i), other.variants.get(i)))
				return false;
		}
		return true;
	}

	JsonObject toJson() {
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

	static void fromJson(ColorConfig config, JsonObject json) {
		var type = ProductType.of(Json.getString(json, "type"))
				.orElse(null);
		if (type == null)
			return;
		var group = config.groupOf(type);
		group.setBase(ColorConfig.rgbOf(json.get("base")));
		var variants = Json.getArray(json, "variants");
		if (variants == null)
			return;
		for (int i = 0; i < variants.size(); i++) {
			var e = variants.get(i);
			group.setVariant(i, ColorConfig.rgbOf(e));
		}
	}
}
