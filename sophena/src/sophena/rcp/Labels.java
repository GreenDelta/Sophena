package sophena.rcp;

import java.time.Month;
import java.time.MonthDay;

import org.eclipse.swt.graphics.Image;

import sophena.model.Boiler;
import sophena.model.BuildingType;
import sophena.model.FuelSpec;
import sophena.model.Producer;
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
		case PIPE:
			return "Wärmeleitung";
		case HEATING_NET_TECHNOLOGY:
			return "Wärmenetz-Technik";
		case HEATING_NET_CONSTRUCTION:
			return "Wärmenetz-Bau";
		case PLANNING:
			return "Planung";
		case TRANSFER_STATION:
			return "Wärmeübergabestation";
		default:
			return null;
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
		case PIPE:
			return "Wärmeleitungen";
		case HEATING_NET_TECHNOLOGY:
			return "Wärmenetz-Technik";
		case HEATING_NET_CONSTRUCTION:
			return "Wärmenetz-Bau";
		case PLANNING:
			return "Planung";
		case TRANSFER_STATION:
			return "Wärmeübergabestationen";
		default:
			return get(t);
		}
	}

	public static Image getImage(ProductType t) {
		if (t == null)
			return null;
		switch (t) {
		case BIOMASS_BOILER:
			return Icon.PRODUCER_16.img();
		case FOSSIL_FUEL_BOILER:
			return Icon.PRODUCER_16.img();
		case COGENERATION_PLANT:
			return Icon.CO_GEN_16.img();
		case BOILER_ACCESSORIES:
			return null;
		case BUFFER_TANK:
			return Icon.BUFFER_16.img();
		case HEAT_RECOVERY:
			return Icon.HEAT_RECOVERY_16.img();
		case FLUE_GAS_CLEANING:
			return Icon.FLUE_GAS_16.img();
		case BOILER_HOUSE_TECHNOLOGY:
			return null;
		case BUILDING:
			return Icon.BUILDING_TYPE_16.img();
		case PIPE:
			return Icon.PIPE_16.img();
		case HEATING_NET_TECHNOLOGY:
			return null;
		case HEATING_NET_CONSTRUCTION:
			return null;
		case PLANNING:
			return null;
		case TRANSFER_STATION:
			return Icon.CONSUMER_16.img();
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

	public static String getFuelUnit(Producer producer) {
		if (producer == null || producer.boiler == null)
			return null;
		if (producer.boiler.fuel != null) {
			// no wood fuel
			return producer.boiler.fuel.unit;
		}
		WoodAmountType type = producer.boiler.woodAmountType;
		return type == null ? null : type.getUnit();
	}

	public static String getFuel(Producer producer) {
		if (producer == null)
			return null;
		Boiler b = producer.boiler;
		if (b != null && b.fuel != null)
			return b.fuel.name;
		FuelSpec fs = producer.fuelSpec;
		if (fs != null && fs.woodFuel != null)
			return fs.woodFuel.name;
		else
			return null;
	}

}
