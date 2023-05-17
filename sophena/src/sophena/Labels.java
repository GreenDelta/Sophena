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
		return switch (fg) {
			case BIOGAS -> "Biogas";
			case NATURAL_GAS -> "Erdgas";
			case LIQUID_GAS -> "Flüssiggas";
			case HEATING_OIL -> "Heizöl";
			case PELLETS -> "Pellets";
			case ELECTRICITY -> "Strom";
			case WOOD -> "Holz";
			case HOT_WATER -> "Warmwasser";
			case PLANTS_OIL -> "Pflanzenöl";
		};
	}

	public static String get(WoodAmountType type) {
		if (type == null)
			return "";
		return switch (type) {
			case CHIPS -> "Holzhackschnitzel";
			case LOGS -> "Holzscheite";
			case MASS -> "Masse";
		};
	}

	public static String get(ProducerFunction fn) {
		if (fn == null)
			return "";
		return switch (fn) {
			case BASE_LOAD -> "Grundlast";
			case PEAK_LOAD -> "Spitzenlast";
		};
	}

	public static String get(BuildingType t) {
		if (t == null)
			return null;
		return switch (t) {
			case SINGLE_FAMILY_HOUSE -> "Einfamilienhaus";
			case MULTI_FAMILY_HOUSE -> "Mehrfamilienhaus";
			case BLOCK_OF_FLATS -> "Wohnblock";
			case TERRACE_HOUSE -> "Reihenhaus";
			case TOWER_BLOCK -> "Hochhaus";
			case SCHOOL -> "Schule";
			case KINDERGARDEN -> "Kindergarten";
			case OFFICE_BUILDING -> "Büro/Rathaus";
			case HOSPITAL -> "Krankenhaus";
			case NURSING_HOME -> "Alten/-Pflegeheim";
			case RESTAURANT -> "Gaststätte";
			case HOTEL -> "Hotel";
			case COMMERCIAL_BUILDING -> "Gewerbe";
			case OTHER -> "Sonstiges";
			case FERMENTER -> "Fermenter";
		};
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
		return switch (t) {
			case BIOMASS_BOILER -> "Biomassekessel";
			case FOSSIL_FUEL_BOILER -> "Fossiler Kessel";
			case HEAT_PUMP -> "Wärmepumpe";
			case COGENERATION_PLANT -> "KWK-Anlage";
			case SOLAR_THERMAL_PLANT -> "Solarthermische Anlage";
			case ELECTRIC_HEAT_GENERATOR -> "Elektrischer Wärmeerzeuger";
			case OTHER_HEAT_SOURCE -> "Sonstige Wärmequelle";
			case BOILER_ACCESSORIES -> "Kesselzubehör";
			case OTHER_EQUIPMENT -> "Sonstiges Zubehör";
			case BUFFER_TANK -> "Pufferspeicher";
			case HEAT_RECOVERY -> "Wärmerückgewinnung";
			case FLUE_GAS_CLEANING -> "Rauchgasreinigung";
			case BOILER_HOUSE_TECHNOLOGY -> "Heizhaus-Technik";
			case BUILDING -> "Gebäude";
			case PIPE -> "Wärmeleitung";
			case HEATING_NET_TECHNOLOGY -> "Wärmenetz-Technik";
			case HEATING_NET_CONSTRUCTION -> "Wärmenetz-Bau";
			case PLANNING -> "Planung";
			case TRANSFER_STATION -> "Wärmeübergabe";
		};
	}

	public static String get(ProductArea area) {
		if (area == null)
			return null;
		return switch (area) {
			case BUILDINGS -> "Gebäude";
			case HEATING_NET -> "Wärmenetz";
			case PLANNING -> "Planung";
			case TECHNOLOGY -> "Anlagentechnik";
		};
	}

	public static String get(Month month) {
		if (month == null)
			return "";
		return switch (month) {
			case JANUARY -> "Januar";
			case FEBRUARY -> "Februar";
			case MARCH -> "März";
			case APRIL -> "April";
			case MAY -> "Mai";
			case JUNE -> "Juni";
			case JULY -> "Juli";
			case AUGUST -> "August";
			case SEPTEMBER -> "September";
			case OCTOBER -> "Oktober";
			case NOVEMBER -> "November";
			case DECEMBER -> "Dezember";
		};
	}

	public static String get(MonthDay md) {
		if (md == null)
			return "";
		return md.getDayOfMonth() + ". " + get(md.getMonth());
	}

	public static String getPlural(ProductType t) {
		if (t == null)
			return null;
		return switch (t) {
			case BIOMASS_BOILER -> "Biomassekessel";
			case FOSSIL_FUEL_BOILER -> "Fossile Kessel";
			case HEAT_PUMP -> "Wärmepumpen";
			case COGENERATION_PLANT -> "KWK-Anlagen";
			case SOLAR_THERMAL_PLANT -> "Solarthermische Anlagen";
			case ELECTRIC_HEAT_GENERATOR -> "Elektrische Wärmeerzeuger";
			case OTHER_HEAT_SOURCE -> "Sonstige Wärmequellen";
			case BOILER_ACCESSORIES -> "Kesselzubehör";
			case OTHER_EQUIPMENT -> "Sonstiges Zubehör";
			case BUFFER_TANK -> "Pufferspeicher";
			case HEAT_RECOVERY -> "Wärmerückgewinnung";
			case FLUE_GAS_CLEANING -> "Rauchgasreinigung";
			case BOILER_HOUSE_TECHNOLOGY -> "Heizhaus-Technik";
			case BUILDING -> "Gebäude";
			case PIPE -> "Wärmeleitungen";
			case HEATING_NET_TECHNOLOGY -> "Wärmenetz-Technik";
			case HEATING_NET_CONSTRUCTION -> "Wärmenetz-Bau";
			case PLANNING -> "Planung";
			case TRANSFER_STATION -> "Wärmeübergabe";
		};
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
