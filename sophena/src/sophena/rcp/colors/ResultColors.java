package sophena.rcp.colors;

import org.eclipse.swt.graphics.Color;
import sophena.calc.ProjectResult;
import sophena.model.Producer;
import sophena.model.ProductType;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ResultColors {

	private final ColorConfig config;
	private final Map<Producer, Color> colors = new HashMap<>();

	private ResultColors(ProjectResult result) {
		config = ColorConfig.get();

		var producers = result.energyResult != null
				? result.energyResult.producers
				: null;
		if (producers != null) {
			var index = new EnumMap<ProductType, Integer>(ProductType.class);
			for (var p : producers) {
				var type = typeOf(p);
				var group = config.groupOf(type);
				int i = index.compute(
						type, ($, v) -> v == null ? 0 : v + 1);
				var color = i == 0
						? Colors.of(group.base())
						: Colors.of(group.variant(i - 1));
				colors.put(p, color);
			}
		}
	}

	private ProductType typeOf(Producer p) {
		if (p == null || p.productGroup == null)
			return ProductType.FOSSIL_FUEL_BOILER;
		var type = p.productGroup.type;
		return type != null
				? type
				: ProductType.FOSSIL_FUEL_BOILER;
	}

	public static ResultColors of(ProjectResult result) {
		return new ResultColors(result);
	}

	public Color of(ColorKey key) {
		return Colors.of(config.get(key));
	}

	public Color of(Producer p) {
		if (p == null)
			return Colors.getBlack();
		var color = colors.get(p);
		return color != null
				? color
				: Colors.getBlack();
	}
}

