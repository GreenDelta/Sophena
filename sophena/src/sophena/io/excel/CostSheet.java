package sophena.io.excel;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Workbook;

import sophena.Labels;
import sophena.calc.CostResult;
import sophena.calc.CostResultItem;
import sophena.calc.ProductAreaResult;
import sophena.calc.ProjectResult;
import sophena.model.ProductArea;
import sophena.utils.Enums;
import sophena.utils.Strings;

class CostSheet {

	private final ProjectResult result;
	private final SheetWriter w;

	CostSheet(Workbook wb, ProjectResult result) {
		this.result = result;
		w = new SheetWriter(wb, "Wirtschaftlichkeit");
	}

	void write() {
		overview(result.costResultFunding, true);
		w.nextRow().nextRow();
		productAreas(result.costResultFunding);
		w.nextRow().nextRow();
		details(result.costResultFunding);
		w.nextRow().nextRow();
		overview(result.costResult, false);
		Excel.autoSize(w.sheet, 0, 5);
	}

	private void overview(CostResult r, boolean withFunding) {
		CostResult.FieldSet dyn = r.dynamicTotal;
		CostResult.FieldSet stat = r.staticTotal;

		String suffix = withFunding ? "" : " - ohne Förderung";
		w.boldStr("Wirtschaftlichkeit" + suffix)
				.nextRow()
				.nextCol()
				.boldStr("Dynamisch")
				.boldStr("Statisch")
				.nextRow();

		w.str("Investitionskosten [EUR]")
				.rint(dyn.investments)
				.rint(stat.investments)
				.nextRow();

		if (withFunding) {
			w.str("Investitionsförderung [EUR]")
					.rint(dyn.funding)
					.rint(stat.funding)
					.nextRow();
		}

		double conFees = result.project.costSettings.connectionFees;
		w.str("Anschlusskostenbeiträge [EUR]")
				.rint(conFees)
				.rint(conFees)
				.nextRow();

		w.boldStr("Finanzierungsbedarf [EUR]")
				.boldRint(dyn.investments - dyn.funding - conFees)
				.boldRint(stat.investments - stat.funding - conFees)
				.nextRow()
				.nextRow();

		w.str("Kapitalgebundene Kosten [EUR/a]")
				.rint(dyn.capitalCosts)
				.rint(stat.capitalCosts)
				.nextRow();

		w.str("Bedarfsgebundene Kosten [EUR/a]")
				.rint(dyn.consumptionCosts)
				.rint(stat.consumptionCosts)
				.nextRow();

		w.str("Betriebsgebundene Kosten [EUR/a]")
				.rint(dyn.operationCosts)
				.rint(stat.operationCosts)
				.nextRow();

		w.str("Sonstige Kosten [EUR/a]")
				.rint(dyn.otherAnnualCosts)
				.rint(stat.otherAnnualCosts)
				.nextRow();

		w.boldStr("Gesamtkosten [EUR/a]")
				.boldRint(dyn.totalAnnualCosts)
				.boldRint(stat.totalAnnualCosts)
				.nextRow()
				.nextRow();

		w.str("Wärmeerlöse [EUR/a]")
				.rint(dyn.revenuesHeat)
				.rint(stat.revenuesHeat)
				.nextRow();

		w.str("Stromerlöse [EUR/a]")
				.rint(dyn.revenuesElectricity)
				.rint(stat.revenuesElectricity)
				.nextRow();

		w.boldStr("Gesamterlöse [EUR/a]")
				.boldRint(dyn.revenuesHeat + dyn.revenuesElectricity)
				.boldRint(stat.revenuesHeat + stat.revenuesElectricity)
				.nextRow()
				.nextRow();

		w.boldStr("Jahresüberschuss [EUR/a]")
				.boldRint(dyn.annualSurplus)
				.boldRint(stat.annualSurplus)
				.nextRow();
		w.boldStr("Wärmegestehungskosten [EUR/MWh]")
				.boldRint(dyn.heatGenerationCosts)
				.boldRint(stat.heatGenerationCosts)
				.nextRow();
	}

	private void productAreas(CostResult r) {
		ProductAreaResult par = ProductAreaResult.calculate(r);

		// select only the product areas that have a result
		List<ProductArea> selected = new ArrayList<>();
		for (ProductArea area : ProductArea.values()) {
			if (par.investmentCosts(area) == 0.0
					&& par.capitalCosts(area) == 0.0
					&& par.demandRelatedCosts(area) == 0.0
					&& par.operationRelatedCosts(area) == 0.0)
				continue;
			selected.add(area);
		}

		w.boldStr("Kostenübersicht")
				.nextRow()
				.boldStr("Produktgebiet")
				.boldStr("Investitionskosten [EUR]")
				.boldStr("Kapitalgebundene Kosten [EUR/a]")
				.boldStr("Bedarfsgebundene Kosten [EUR/a]")
				.boldStr("Betriebsgebundene Kosten [EUR/a]")
				.nextRow();

		for (ProductArea a : selected) {
			w.str(Labels.get(a))
					.rint(par.investmentCosts(a))
					.rint(par.capitalCosts(a))
					.rint(par.demandRelatedCosts(a))
					.rint(par.operationRelatedCosts(a))
					.nextRow();
		}
	}

	private void details(CostResult r) {
		w.boldStr("Kostendetails")
				.nextRow()
				.boldStr("Produktbereich")
				.boldStr("Produkt")
				.boldStr("Investitionskosten [EUR]")
				.boldStr("Kapitalgebundene Kosten [EUR/a]")
				.boldStr("Bedarfsgebundene Kosten [EUR/a]")
				.boldStr("Betriebsgebundene Kosten [EUR/a]")
				.nextRow();

		// sort the result items first by product type
		// then by product name
		r.items.sort((i1, i2) -> {
			int c = Enums.compare(i1.productType, i2.productType);
			if (c != 0)
				return c;
			return Strings.compare(i1.label, i2.label);
		});

		String category = "";
		for (CostResultItem i : r.items) {

			// write the product type only if when
			// previous was different
			String c = Labels.getPlural(i.productType);
			if (Strings.nullOrEqual(c, category)) {
				w.nextCol();
			} else {
				category = c;
				w.boldStr(category);
			}

			w.str(i.label)
					.rint(i.investmentCosts)
					.rint(i.capitalCosts)
					.rint(i.demandRelatedCosts)
					.rint(i.operationRelatedCosts)
					.nextRow();
		}
	}

}
