package sophena;

import java.time.Month;
import java.time.MonthDay;

import sophena.model.BuildingType;
import sophena.model.FuelGroup;
import sophena.model.FuelSpec;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.ProductArea;
import sophena.model.ProductType;
import sophena.model.WoodAmountType;
import sophena.utils.Strings;

public final class Labels {

	private Labels() {
	}

	public static String get(FuelGroup fg) {
		if (fg == null)
			return null;
		switch (fg) {
		case BIOGAS:
			return "Biogas";
		case NATURAL_GAS:
			return "Erdgas";
		case LIQUID_GAS:
			return "Flüssiggas";
		case HEATING_OIL:
			return "Heizöl";
		case PELLETS:
			return "Pellets";
		case ELECTRICITY:
			return "Strom";
		case WOOD:
			return "Holz";
		case HOT_WATER:
			return "Warmwasser";
		case PLANTS_OIL:
			return "Pflanzenöl";
		default:
			return "";
		}
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
		case SCHOOL:
			return "Schule";
		case KINDERGARDEN:
			return "Kindergarten";
		case OFFICE_BUILDING:
			return "Büro/Rathaus";
		case HOSPITAL:
			return "Krankenhaus";
		case NURSING_HOME:
			return "Alten/-Pflegeheim";
		case RESTAURANT:
			return "Gaststätte";
		case HOTEL:
			return "Hotel";
		case COMMERCIAL_BUILDING:
			return "Gewerbe";
		case OTHER:
			return "Sonstiges";
		case FERMENTER:
			return "Fermenter";
		default:
			return "";
		}
	}

	public static BuildingType getBuildingType(String label) {
		if (label == null)
			return null;
		for (BuildingType t : BuildingType.values()) {
			if (Strings.nullOrEqual(label, get(t)))
				return t;
		}
		return BuildingType.OTHER;
	}

	public static String get(ProductType t) {
		if (t == null)
			return null;
		switch (t) {
		case BIOMASS_BOILER:
			return "Biomassekessel";
		case FOSSIL_FUEL_BOILER:
			return "Fossiler Kessel";
		case HEAT_PUMP:
			return "Wärmepumpe";
		case COGENERATION_PLANT:
			return "KWK-Anlage";
		case SOLAR_THERMAL_PLANT:
			return "Solarthermische Anlage";
		case ELECTRIC_HEAT_GENERATOR:
			return "Elektrischer Wärmeerzeuger";
		case OTHER_HEAT_SOURCE:
			return "Sonstige Wärmequelle";
		case BOILER_ACCESSORIES:
			return "Kesselzubehör";
		case OTHER_EQUIPMENT:
			return "Sonstiges Zubehör";
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
		case PIPE:
			return "Wärmeleitung";
		case HEATING_NET_TECHNOLOGY:
			return "Wärmenetz-Technik";
		case HEATING_NET_CONSTRUCTION:
			return "Wärmenetz-Bau";
		case PLANNING:
			return "Planung";
		case TRANSFER_STATION:
			return "Wärmeübergabe";
		default:
			return null;
		}
	}

	public static String get(ProductArea area) {
		if (area == null)
			return null;
		switch (area) {
		case BUILDINGS:
			return "Gebäude";
		case HEATING_NET:
			return "Wärmenetz";
		case PLANNING:
			return "Planung";
		case TECHNOLOGY:
			return "Anlagentechnik";
		default:
			return "?";
		}
	}

	public static String get(Month month) {
		if (month == null)
			return "";
		switch (month) {
		case JANUARY:
			return "Januar";
		case FEBRUARY:
			return "Februar";
		case MARCH:
			return "März";
		case APRIL:
			return "April";
		case MAY:
			return "Mai";
		case JUNE:
			return "Juni";
		case JULY:
			return "Juli";
		case AUGUST:
			return "August";
		case SEPTEMBER:
			return "September";
		case OCTOBER:
			return "Oktober";
		case NOVEMBER:
			return "November";
		case DECEMBER:
			return "Dezember";
		default:
			return "";
		}
	}

	public static String get(MonthDay md) {
		if (md == null)
			return "";
		return md.getDayOfMonth() + ". " + get(md.getMonth());
	}

	public static String getPlural(ProductType t) {
		if (t == null)
			return null;
		switch (t) {
		case BIOMASS_BOILER:
			return "Biomassekessel";
		case FOSSIL_FUEL_BOILER:
			return "Fossile Kessel";
		case HEAT_PUMP:
			return "Wärmepumpen";
		case COGENERATION_PLANT:
			return "KWK-Anlagen";
		case SOLAR_THERMAL_PLANT:
			return "Solarthermische Anlagen";
		case ELECTRIC_HEAT_GENERATOR:
			return "Elektrische Wärmeerzeuger";
		case OTHER_HEAT_SOURCE:
			return "Sonstige Wärmequellen";
		case BOILER_ACCESSORIES:
			return "Kesselzubehör";
		case OTHER_EQUIPMENT:
			return "Sonstiges Zubehör";
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
		case PIPE:
			return "Wärmeleitungen";
		case HEATING_NET_TECHNOLOGY:
			return "Wärmenetz-Technik";
		case HEATING_NET_CONSTRUCTION:
			return "Wärmenetz-Bau";
		case PLANNING:
			return "Planung";
		case TRANSFER_STATION:
			return "Wärmeübergabe";
		default:
			return get(t);
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

	public static String getFuelUnit(Producer producer) {
		if (producer == null || producer.fuelSpec == null)
			return "?";
		FuelSpec spec = producer.fuelSpec;
		if (spec.woodAmountType != null)
			return spec.woodAmountType.getUnit();
		if (spec.fuel == null)
			return "?";
		return spec.fuel.unit != null ? spec.fuel.unit : "?";
	}

	public static String getFuel(Producer producer) {
		if (producer == null || producer.fuelSpec == null)
			return "?";
		FuelSpec spec = producer.fuelSpec;
		return spec.fuel != null ? spec.fuel.name : "?";
	}
}
