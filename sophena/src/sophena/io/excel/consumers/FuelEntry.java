package sophena.io.excel.consumers;

import sophena.db.Database;
import sophena.db.daos.FuelDao;
import sophena.math.energetic.UtilisationRate;
import sophena.model.FuelConsumption;
import sophena.model.WoodAmountType;
import sophena.utils.Num;
import sophena.utils.Result;

import java.util.Optional;
import java.util.UUID;

class FuelEntry {

	private final int row;

	private String fuel;
	private Double fuelAmount;
	private String fuelUnit;
	private Double waterFraction;
	private Double efficiencyRate;
	private Double utilisationRate;

	private FuelEntry(int row) {
		this.row = row;
	}

	static Optional<FuelEntry> readFrom(RowReader r) {
		if (r == null)
			return Optional.empty();
		var e = new FuelEntry(r.index() + 1);
		e.fuel = r.str(Field.FUEL);
		e.fuelAmount = r.num(Field.FUEL_AMOUNT);
		e.fuelUnit = r.str(Field.FUEL_UNIT);
		e.waterFraction = r.num(Field.WATER_FRACTION);
		e.efficiencyRate = r.num(Field.EFFICIENCY_RATE);
		e.utilisationRate = r.num(Field.UTILIZATION_RATE);
		return Optional.of(e);
	}

	Result<FuelConsumption> toFuelConsumption(Database db, int loadHours) {
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

		// sync. water content for wood fuels
		if (fuelObj.isWood()) {
			if (waterFraction == null)
				return err("es wurde kein Wassergehalt angegeben");
			cons.waterContent = waterFraction;
			if (cons.waterContent < 0 || cons.waterContent > 0.6)
				return err("ungültiger Wassergehalt: " + Num.str(cons.waterContent));
			cons.waterContent *= 100;
		}

		// sync. utilisation rate or efficiency rate
		if (utilisationRate != null) {
			double ur = utilisationRate;
			if (ur < 0 || ur > 10)
				return err("ungültiger Nutzungsgrad: " + Num.str(ur));
			cons.utilisationRate = ur * 100;
		} else if (efficiencyRate != null) {
			double er = efficiencyRate;
			if (er < 0 || er > 10)
				return err("ungültiger Wirkungsgrad: " + Num.str(er));
			cons.utilisationRate = UtilisationRate.get(er * 100, loadHours);
		}

		if (efficiencyRate == null && utilisationRate == null)
			return err("es wurde kein Wirkungsgrad oder Nutzungsgrad angegeben");

		return Result.ok(cons);
	}

	private Result<FuelConsumption> err(String msg) {
		return Result.error("Zeile " + row + ": " + msg);
	}

	private boolean eq(String s1, String s2) {
		return s1 != null
				&& s2 != null
				&& s1.strip().equalsIgnoreCase(s2.strip());
	}

	private WoodAmountType woodAmountTypeOf(String unit) {
		for (var wat : WoodAmountType.values()) {
			if (eq(wat.getUnit(), unit))
				return wat;
		}
		return null;
	}
}
