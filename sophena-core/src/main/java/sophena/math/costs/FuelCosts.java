package sophena.math.costs;

import sophena.calc.ProjectResult;
import sophena.model.CostSettings;
import sophena.model.Fuel;
import sophena.model.FuelGroup;
import sophena.model.FuelSpec;
import sophena.model.Producer;
import sophena.model.WoodAmountType;

/**
 * Functions for calculating costs related to fuel consumption.
 */
public class FuelCosts {

	private FuelCosts() {
	}

	public static double gross(ProjectResult r, Producer p) {
		double net = net(r, p);
		if (p.fuelSpec == null)
			return 0;
		r.calcLog.println("=> Brennstoffkosten (brutto): " + p.name);
		r.calcLog.value("Cn: Brennstoffkosten (netto)", net, "EUR");
		double vat = p.fuelSpec.taxRate / 100;
		r.calcLog.value("vat: Mehrwertsteuer", net, "EUR");
		double gross = net * (1 + vat);
		r.calcLog.value("Cb: Brennstoffkosten (brutto): Cb = Cn * (1 + vat)",
				gross, "EUR");
		return gross;
	}

	public static double net(ProjectResult r, Producer p) {
		if (r == null || p == null || p.fuelSpec == null)
			return 0;
		FuelSpec spec = p.fuelSpec;
		r.calcLog.println("=> Brennstoffkosten: " + p.name);
		double amount = r.fuelUsage.getInFuelUnits(p);
		r.calcLog.value("F: Brennstoffmenge", amount, spec.getUnit());
		double price = spec.pricePerUnit;
		r.calcLog.value("p: Preis pro Einheit", price, "EUR/" + spec.getUnit());
		double val = amount * price;
		r.calcLog.value("C: Brennstoffkosten: C = F * p", val, "EUR");
		r.calcLog.println();
		return val;
	}

	public static double getPriceChangeFactor(Producer p,
			CostSettings settings) {
		if (p == null || p.fuelSpec == null || settings == null)
			return settings.fossilFuelFactor;
		Fuel fuel = p.fuelSpec.fuel;
		if (fuel == null || fuel.group == null)
			return settings.fossilFuelFactor;
		switch (fuel.group) {
		case BIOGAS:
		case PELLETS:
		case PLANTS_OIL:
		case WOOD:
			return settings.bioFuelFactor;
		case ELECTRICITY:
			return settings.electricityFactor;
		default:
			return settings.fossilFuelFactor;
		}
	}

	public static double netAshCosts(ProjectResult r, Producer p) {
		if (p == null)
			return 0d;
		FuelSpec spec = p.fuelSpec;
		if (spec == null || spec.fuel == null
				|| spec.ashCosts <= 0 || spec.fuel.ashContent <= 0)
			return 0d;
		r.calcLog.println("=> Ascheentsorgungskosten: " + p.name);
		r.calcLog.value("c: Ascheentsorgungskosten pro Tonne", spec.ashCosts,
				"EUR/t");
		double fuelAmount = r.fuelUsage.getInFuelUnits(p);
		r.calcLog.value("F: Brennstoffmenge", fuelAmount, spec.getUnit());
		double ashContent = spec.fuel.ashContent / 100;
		r.calcLog.value("ca: Aschegehalt", ashContent, "");

		// handle non-wood fuels
		if (spec.fuel.group != FuelGroup.WOOD || spec.woodAmountType == null) {
			// we assume that the fuel amount is given in tons
			double val = fuelAmount * ashContent * spec.ashCosts;
			r.calcLog.value("Ascheentsorgungskosten: C = F * ca * c", val,
					"EUR");
			r.calcLog.println();
			return val;
		}

		double w = spec.waterContent / 100d;
		r.calcLog.value("w: Wassergehalt", w, "");

		// handle wood fuels
		double wetTons = 0d;
		if (spec.woodAmountType == WoodAmountType.MASS) {
			wetTons = fuelAmount;
			r.calcLog.value("Ff: Feuchtmasse", fuelAmount, "t");
		} else {
			double f = spec.woodAmountType.getFactor();
			r.calcLog.value("f: Faktor für Holzmasse", f, "");
			double rho = spec.fuel.density / 1000;
			r.calcLog.value("rho: Dichte", rho, "t/FM");
			wetTons = fuelAmount * (f * rho / (1 - w));
			r.calcLog.value("Ff: Feuchtmasse", wetTons, "t");
		}

		double dryTons = (1d - w) * wetTons;
		r.calcLog.value("Fd: Trockenmasse: (1 - w) * Ff", dryTons, "t");

		double val = dryTons * ashContent * spec.ashCosts;
		r.calcLog.value("Ascheentsorgungskosten: C = Fd * ca * c", val,
				"EUR");
		r.calcLog.println();
		return val;
	}
}
