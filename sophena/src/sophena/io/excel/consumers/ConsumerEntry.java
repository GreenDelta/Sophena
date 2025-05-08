package sophena.io.excel.consumers;

import sophena.Labels;
import sophena.db.Database;
import sophena.db.daos.BuildingStateDao;
import sophena.model.BuildingType;
import sophena.model.Consumer;
import sophena.model.Location;
import sophena.utils.Num;
import sophena.utils.Result;
import sophena.utils.Strings;

import java.util.ArrayList;
import java.util.List;
import java.text.DecimalFormat;
import java.util.Optional;
import java.util.UUID;

class ConsumerEntry {

	private final int index;

	private String name;
	private String description;
	private String buildingType;
	private String buildingState;

	private String type;
	private Double heatingLoad;
	private final List<FuelEntry> fuels = new ArrayList<>();

	private String location;
	private String street;
	private String zipCode;
	private String city;
	private Double latitude;
	private Double longitude;

	private ConsumerEntry(int index) {
		this.index = index;
	}

	static Optional<ConsumerEntry> readFrom(RowReader r) {
		if (r == null)
			return Optional.empty();
		var name = r.str(Field.NAME);
		if (name == null)
			return Optional.empty();

		var row = new ConsumerEntry(r.index() + 1);
		row.name = name;
		row.description = r.str(Field.DESCRIPTION);
		row.buildingType = r.str(Field.BUILDING_TYPE);
		row.buildingState = r.str(Field.BUILDING_STATE);

		row.type = r.str(Field.TYPE);
		row.heatingLoad = r.num(Field.HEATING_LOAD);

		row.location = r.str(Field.LOCATION);
		row.street = r.str(Field.STREET);
		row.zipCode = r.str(Field.ZIP_CODE, new DecimalFormat("#"));
		row.city = r.str(Field.CITY);
		row.latitude = r.num(Field.LATITUDE);
		row.longitude = r.num(Field.LONGITUDE);

		return Optional.of(row);
	}

	Result<Consumer> toConsumer(Database db) {
		if (name == null)
			return err("es wurde kein Name für den Abnehmer angegeben");

		var consumer = new Consumer();
		consumer.id = UUID.randomUUID().toString();
		consumer.name = name;
		consumer.description = description;
		consumer.location = mapLocation();

		// sync. building state and consumer type
		var r = syncBuildingState(consumer, db);
		if (r.isError())
			return r;
		r = syncType(consumer);
		if (r.isError())
			return r;

		// sync. heating load or fuel consumption
		return consumer.demandBased
				? syncHeatingLoad(consumer)
				: syncFuelConsumptions(consumer, db);
	}


	private Result<Consumer> syncBuildingState(Consumer c, Database db) {

		// sync. building type
		if (buildingType == null)
			return err("es wurde kein Gebäudetyp angegeben");
		var type = buildingTypeOf(buildingType);
		if (type == null)
			return err("unbekannter Gebäudetyp: " + buildingType);

		var states = new BuildingStateDao(db).getAll()
				.stream()
				.filter(s -> s.type == type)
				.toList();
		if (states.isEmpty())
			return err("Gebäudetyp " + buildingType
					+ " hat keinen Defaultzustand in der Datenbank");

		var state = states.size() == 1
				? states.get(0)
				: states.stream()
				.filter(s -> eq(buildingState, s.name))
				.findAny()
				.orElse(null);

		// sync. building state
		if (state == null)
			return Strings.nullOrEmpty(buildingState)
					? err("es wurde kein Gebäudezustand angegeben")
					: err("unbekannter Gebäudezustand: " + buildingState);

		c.buildingState = state;
		c.heatingLimit = state.heatingLimit;
		c.waterFraction = state.waterFraction;
		c.loadHours = state.loadHours;
		return Result.ok(c);
	}

	private BuildingType buildingTypeOf(String s) {
		if (s == null)
			return null;
		for (var type : BuildingType.values()) {
			if (eq(Labels.get(type), s))
				return type;
		}
		return null;
	}

	private Result<Consumer> syncType(Consumer c) {
		if (type == null)
			return err("die Art des Abnehmers wurde nicht definiert");
		if (eq(type, "v") || eq(type, "verbrauchsgebunden")) {
			c.demandBased = false;
			return Result.ok(c);
		}
		if (eq(type, "b") || eq(type, "bedarfsgebunden")) {
			c.demandBased = true;
			return Result.ok(c);
		}
		return err("unbekannte Art des Abnehmers: " + type);
	}

	private Result<Consumer> syncHeatingLoad(Consumer c) {
		if (heatingLoad == null)
			return err("es wurde keine Heizlast angegeben");
		double hl = heatingLoad;
		if (hl <= 0)
			return err("ungültige Heizlast: " + Num.str(hl));
		c.heatingLoad = hl;
		return Result.ok(c);
	}

	private Result<Consumer> syncFuelConsumptions(Consumer c, Database db) {
		double usedHeat = 0;
		for (var e : fuels) {
			var r = e.toFuelConsumption(db, c.loadHours);
			if (r.isError())
				return err(r.message()
						.orElse("Brennstoffverbrauch konnte nicht gelesen werden"));
			var fuelCons = r.get();
			usedHeat += fuelCons.getUsedHeat();
			c.fuelConsumptions.add(fuelCons);
		}
		if (c.loadHours != 0) {
			c.heatingLoad = usedHeat / c.loadHours;
		}
		return Result.ok(c);
	}

	private boolean eq(String s1, String s2) {
		return s1 != null
				&& s2 != null
				&& s1.strip().equalsIgnoreCase(s2.strip());
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

	boolean isConsumptionBased() {
		var t = type != null
				? type.strip().toLowerCase()
				: "v";
		return !t.equals("b");
	}

	void add(FuelEntry fuelEntry) {
		if (fuelEntry != null) {
			fuels.add(fuelEntry);
		}
	}

	private Result<Consumer> err(String msg) {
		return Result.error("Zeile " + index + ": " + msg);
	}
}
