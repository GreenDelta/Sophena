package sophena.model;

import sophena.utils.Strings;

import java.util.Objects;
import java.util.Optional;

/**
 * All products in Sophena have exactly one of the following types. The order in
 * this enum is equal to the display order in the user interface.
 */
public enum ProductType {

	BIOMASS_BOILER(ProductArea.TECHNOLOGY),

	FOSSIL_FUEL_BOILER(ProductArea.TECHNOLOGY),

	HEAT_PUMP(ProductArea.TECHNOLOGY),

	COGENERATION_PLANT(ProductArea.TECHNOLOGY),

	SOLAR_THERMAL_PLANT(ProductArea.TECHNOLOGY),

	ELECTRIC_HEAT_GENERATOR(ProductArea.TECHNOLOGY),

	OTHER_HEAT_SOURCE(ProductArea.TECHNOLOGY),

	BOILER_ACCESSORIES(ProductArea.TECHNOLOGY),

	OTHER_EQUIPMENT(ProductArea.TECHNOLOGY),

	HEAT_RECOVERY(ProductArea.TECHNOLOGY),

	FLUE_GAS_CLEANING(ProductArea.TECHNOLOGY),

	BUFFER_TANK(ProductArea.TECHNOLOGY),

	BOILER_HOUSE_TECHNOLOGY(ProductArea.TECHNOLOGY),

	BUILDING(ProductArea.BUILDINGS),

	PIPE(ProductArea.HEATING_NET),

	HEATING_NET_TECHNOLOGY(ProductArea.HEATING_NET),

	HEATING_NET_CONSTRUCTION(ProductArea.HEATING_NET),

	TRANSFER_STATION(ProductArea.HEATING_NET),

	PLANNING(ProductArea.PLANNING);

	public final ProductArea productArea;

	ProductType(ProductArea area) {
		this.productArea = area;
	}

	public static Optional<ProductType> of(String name) {
		if (Strings.nullOrEmpty(name))
			return Optional.empty();
		for (var type : values()) {
			if (Objects.equals(type.name(), name))
				return Optional.of(type);
		}
		return Optional.empty();
	}
}
