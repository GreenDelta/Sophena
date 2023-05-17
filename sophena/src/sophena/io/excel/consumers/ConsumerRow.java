package sophena.io.excel.consumers;

import sophena.db.Database;
import sophena.model.BuildingType;
import sophena.model.Consumer;
import sophena.model.Location;
import sophena.utils.Result;

import java.util.Optional;
import java.util.UUID;

class ConsumerRow {

	private final int index;

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

	private ConsumerRow(int index) {
		this.index = index;
	}

	static Optional<ConsumerRow> readFrom(RowReader r) {
		if (r == null)
			return Optional.empty();
		var name = r.str(Field.NAME);
		if (name == null)
			return Optional.empty();

		var row = new ConsumerRow(r.index() + 1);
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


	Result<Consumer> map(Database db) {




		var consumer = new Consumer();
		consumer.id = UUID.randomUUID().toString();
		consumer.name = name;

		var buildingType = mapBuildingType();
		if (buildingType == null)
			return err("unbekannter Gebäudetyp: " );

		consumer.location = mapLocation();
		return Result.ok(consumer);
	}

	private BuildingType mapBuildingType() {
		return null;
	}

	private Location mapLocation() {
		if (location == null
				&& street == null
				&& zipCode == null
				&& city == null
				&& latitude == null
				&& longitude == null)
			return null;
		var loc = new Location();
		loc.id = UUID.randomUUID().toString();
		loc.name = location;
		loc.street = street;
		loc.zipCode = zipCode;
		loc.city = city;
		loc.latitude = latitude;
		loc.longitude = longitude;
		return loc;
	}

	private Result<Consumer> err(String msg) {
		return Result.error("Zeile " + index + ": " + msg);
	}
}
