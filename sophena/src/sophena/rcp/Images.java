package sophena.rcp;

import org.eclipse.swt.graphics.Image;

import sophena.model.ProductType;

public final class Images {

	private Images() {
	}

	public static Image getImage(ProductType t) {
		if (t == null)
			return null;
		switch (t) {
		case BIOMASS_BOILER:
			return Icon.PRODUCER_16.img();
		case FOSSIL_FUEL_BOILER:
			return Icon.PRODUCER_16.img();
		case HEAT_PUMP:
			return Icon.HEAT_PUMP_16.img();
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
		case SOLAR_THERMAL_PLANT:
			return Icon.SOLARTHERM_16.img();
		default:
			return null;
		}
	}
}
