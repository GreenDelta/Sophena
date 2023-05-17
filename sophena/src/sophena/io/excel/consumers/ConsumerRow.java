package sophena.io.excel.consumers;

import java.util.Optional;

class ConsumerRow {

	private String name;
	private String description;
	private String buildingType;
	private String buildingState;
	private String type;

	private Double heatingLoad;
	private String fuel;
	private Double fuelAmount;
	private String fuelUnit;
	private Double waterFraction;
	private Double efficiencyRate;
	private Double utilisationRate;

	private String location;
	private String street;
	private String zipCode;
	private String city;
	private Double latitude;
	private Double longitude;

	private ConsumerRow() {
	}

	static Optional<ConsumerRow> readFrom(RowReader r) {
		if (r == null)
			return Optional.empty();
		var name = r.str(Field.NAME);
		if (name == null)
			return Optional.empty();

		var row = new ConsumerRow();
		row.name = name;
		row.description = r.str(Field.DESCRIPTION);
		row.buildingType = r.str(Field.BUILDING_TYPE);
		row.buildingState = r.str(Field.BUILDING_STATE);
		row.type = r.str(Field.TYPE);

		row.heatingLoad = r.num(Field.HEATING_LOAD);
		row.fuel = r.str(Field.FUEL);
		row.fuelAmount = r.num(Field.FUEL_AMOUNT);
		row.fuelUnit = r.str(Field.FUEL_UNIT);
		row.waterFraction = r.num(Field.WATER_FRACTION);
		row.efficiencyRate = r.num(Field.EFFICIENCY_RATE);
		row.utilisationRate = r.num(Field.UTILIZATION_RATE);

		row.location = r.str(Field.LOCATION);
		row.street = r.str(Field.STREET);
		row.zipCode = r.str(Field.ZIP_CODE);
		row.city = r.str(Field.CITY);
		row.latitude = r.num(Field.LATITUDE);
		row.longitude = r.num(Field.LONGITUDE);

		return Optional.of(row);
	}
}
