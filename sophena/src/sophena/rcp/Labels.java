package sophena.rcp;

import sophena.model.BuildingType;
import sophena.model.ProducerFunction;
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

	public static BuildingType geBuildingType(String label) {
		if (label == null)
			return null;
		for (BuildingType t : BuildingType.values()) {
			if (Strings.nullOrEqual(label, get(t)))
				return t;
		}
		return null;
	}
}
