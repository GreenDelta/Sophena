package sophena.rcp;

import org.eclipse.swt.graphics.Image;

import sophena.model.BuildingType;
import sophena.model.ProducerFunction;
import sophena.model.ProductType;
import sophena.model.WoodAmountType;
import sophena.rcp.utils.Strings;

public final class Labels {

	private Labels() {
	}

	public static String get(WoodAmountType type) {
		if (type == null)
			return "";
		switch (type) {
		case CHIPS:
			return "Holzhackschnitzel";
		case LOGS:
			return "Holzscheite";
		case MASS:
			return "Masse";
		default:
			return "?";
		}
	}

	public static WoodAmountType getWoodAmountType(String label) {
		if (label == null)
			return null;
		for (WoodAmountType t : WoodAmountType.values()) {
			if (Strings.nullOrEqual(label, get(t)))
				return t;
		}
		return null;
	}

	public static String get(ProducerFunction fn) {
		if (fn == null)
			return "";
		switch (fn) {
		case BASE_LOAD:
			return "Grundlast";
		case PEAK_LOAD:
			return "Spitzenlast";
		default:
			return "?";
		}
	}

	public static ProducerFunction getProducerFunction(String label) {
		if (label == null)
			return null;
		for (ProducerFunction fn : ProducerFunction.values()) {
			if (Strings.nullOrEqual(label, get(fn)))
				return fn;
		}
		return null;
	}

	public static String get(BuildingType t) {
		if (t == null)
			return null;
		switch (t) {
		case SINGLE_FAMILY_HOUSE:
			return "Einfamilienhaus";
		case MULTI_FAMILY_HOUSE:
			return "Mehrfamilienhaus";
		case BLOCK_OF_FLATS:
			return "Wohnblock";
		case TERRACE_HOUSE:
			return "Reihenhaus";
		case TOWER_BLOCK:
			return "Hochhaus";
		default:
			return null;
		}
	}

	public static BuildingType getBuildingType(String label) {
		if (label == null)
			return null;
		for (BuildingType t : BuildingType.values()) {
			if (Strings.nullOrEqual(label, get(t)))
				return t;
		}
		return null;
	}

	public static String get(ProductType t) {
		if (t == null)
			return null;
		switch (t) {
		case BIOMASS_BOILER:
			return "Biomassekessel";
		case FOSSIL_FUEL_BOILER:
			return "Fossiler Kessel";
		case COGENERATION_PLANT:
			return "KWK-Anlage";
		case BOILER_ACCESSORIES:
			return "Kesselzubehör";
		case BUFFER_TANK:
			return "Pufferspeicher";
		case HEAT_RECOVERY:
			return "Wärmerückgewinnung";
		case FLUE_GAS_CLEANING:
			return "Rauchgasreinigung";
		case BOILER_HOUSE_TECHNOLOGY:
			return "Heizhaus-Technik";
		case BUILDING:
			return "Gebäude";
		case HEATING_NET_TECHNOLOGY:
			return "Wärmenetz-Technik";
		case HEATING_NET_CONSTRUCTION:
			return "Wärmenetz-Bau";
		case PLANNING:
			return "Planung";
		default:
			return null;
		}
	}

	public static String getPlural(ProductType t) {
		if (t == null)
			return null;
		switch (t) {
		case BIOMASS_BOILER:
			return "Biomassekessel";
		case FOSSIL_FUEL_BOILER:
			return "Fossile Kessel";
		case COGENERATION_PLANT:
			return "KWK-Anlagen";
		case BOILER_ACCESSORIES:
			return "Kesselzubehör";
		case BUFFER_TANK:
			return "Pufferspeicher";
		case HEAT_RECOVERY:
			return "Wärmerückgewinnung";
		case FLUE_GAS_CLEANING:
			return "Rauchgasreinigung";
		case BOILER_HOUSE_TECHNOLOGY:
			return "Heizhaus-Technik";
		case BUILDING:
			return "Gebäude";
		case HEATING_NET_TECHNOLOGY:
			return "Wärmenetz-Technik";
		case HEATING_NET_CONSTRUCTION:
			return "Wärmenetz-Bau";
		case PLANNING:
			return "Planung";
		default:
			return null;
		}
	}

	public static Image getImage(ProductType t) {
		if (t == null)
			return null;
		switch (t) {
		case BIOMASS_BOILER:
			return Images.PRODUCER_16.img();
		case FOSSIL_FUEL_BOILER:
			return Images.PRODUCER_16.img();
		case COGENERATION_PLANT:
			return Images.CO_GEN_16.img();
		case BOILER_ACCESSORIES:
			return null;
		case BUFFER_TANK:
			return Images.BUFFER_16.img();
		case HEAT_RECOVERY:
			return null;
		case FLUE_GAS_CLEANING:
			return null;
		case BOILER_HOUSE_TECHNOLOGY:
			return null;
		case BUILDING:
			return Images.BUILDING_TYPE_16.img();
		case HEATING_NET_TECHNOLOGY:
			return null;
		case HEATING_NET_CONSTRUCTION:
			return null;
		case PLANNING:
			return null;
		default:
			return null;
		}
	}

	public static ProductType getProductType(String label) {
		if (label == null)
			return null;
		for (ProductType t : ProductType.values()) {
			if (Strings.nullOrEqual(label, get(t)))
				return t;
		}
		return null;
	}

}
