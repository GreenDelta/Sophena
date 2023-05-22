package sophena.io.excel.consumers;

import sophena.Labels;
import sophena.db.Database;
import sophena.db.daos.BuildingStateDao;
import sophena.db.daos.FuelDao;
import sophena.math.energetic.UtilisationRate;
import sophena.model.*;
import sophena.utils.Num;
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
				: syncFuelConsumption(consumer, db);
	}


	private Result<Consumer> syncBuildingState(Consumer c, Database db) {

		// sync. building type
		if (buildingType == null)
			return err("es wurde kein Gebäudetyp angegeben");
		var type = buildingTypeOf(buildingType);
		if (type == null)
			return err("unbekannter Gebäudetyp: " + buildingType);

		// sync. building state
		if (buildingState == null)
			return err("es wurde kein Gebäudezustand angegeben");
		var state = new BuildingStateDao(db).getAll()
				.stream()
				.filter(s -> s.type == type && eq(buildingState, s.name))
				.findAny()
				.orElse(null);
		if (state == null)
			return err("unbekannter Gebäudezustand: " + buildingState);

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

	private Result<Consumer> syncFuelConsumption(Consumer c, Database db) {

		// sync. fuel
		if (fuel == null)
			return err("es wurde kein Brennstoff angegeben");
		var fuelObj = new FuelDao(db).getAll()
				.stream()
				.filter(f -> eq(fuel, f.name))
				.findAny()
				.orElse(null);
		if (fuelObj == null)
			return err("unbekannter Brennstoff: " + fuel);

		var cons = new FuelConsumption();
		cons.id = UUID.randomUUID().toString();
		cons.fuel = fuelObj;

		// sync. amount
		if (fuelAmount == null)
			return err("es wurde keine Brennstoffmenge angegeben");
		cons.amount = fuelAmount;
		if (cons.amount <= 0)
			return err("ungültige Brennstoffmenge: " + Num.str(cons.amount));

		// sync. unit
		if (fuelUnit == null)
			return err("es wurde keine Brennstoffeinheit angegeben");
		if (fuelObj.isWood()) {
			cons.woodAmountType = woodAmountTypeOf(fuelUnit);
			if (cons.woodAmountType == null)
				return err("unbekannte Einheit für Holzbrennstoff: " + fuelUnit);
		} else {
			if (!eq(fuelUnit, fuelObj.unit))
				return err("Brennstoff " + fuel + " muss in "
						+ fuelObj.unit + " angegeben werden");
		}

		// sync. water content
		if (fuelObj.isWood()) {
			if (waterFraction == null)
				return err("es wurde kein Wassergehalt angegeben");
			cons.waterContent = waterFraction;
			if (cons.waterContent < 0 || cons.waterContent > 0.6)
				return err("ungültiger Wassergehalt: " + Num.str(cons.waterContent));
		}

		// sync. utilisation rate
		if (utilisationRate != null) {
			double ur = utilisationRate;
			if (ur < 0 || ur > 10)
				return err("ungültiger Nutzungsgrad: " + Num.str(ur));
			cons.utilisationRate = ur * 100;
		} else if (efficiencyRate != null) {
			double er = efficiencyRate;
			if (er < 0 || er > 10)
				return err("ungültiger Wirkungsgrad: " + Num.str(er));
			cons.utilisationRate = UtilisationRate.get(er * 100, c.loadHours);
		}

		if (efficiencyRate == null && utilisationRate == null)
			return err("es wurde kein Wirkungsgrad oder Nutzungsgrad angegeben");


		c.fuelConsumptions.add(cons);
		return Result.ok(c);
	}

	private WoodAmountType woodAmountTypeOf(String unit) {
		for (var wat : WoodAmountType.values()) {
			if (eq(wat.getUnit(), unit))
				return wat;
		}
		return null;
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

	private Result<Consumer> err(String msg) {
		return Result.error("Zeile " + index + ": " + msg);
	}
}
