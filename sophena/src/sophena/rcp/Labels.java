package sophena.rcp;

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
		for (WoodAmountType t : WoodAmountType.values()) {
			if (Strings.nullOrEqual(label, get(t)))
				return t;
		}
		return null;
	}

}
