package sophena.io.excel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.function.Consumer;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.Labels;
import sophena.calc.kpi.CO2Result;
import sophena.calc.Comparison;
import sophena.calc.costs.CostResult.Summary;
import sophena.calc.costs.ProductAreaResult;
import sophena.calc.ProjectResult;
import sophena.calc.kpi.EfficiencyResult;
import sophena.calc.kpi.PrimaryEnergyFactor;
import sophena.calc.specs.Producers;
import sophena.calc.kpi.UsedHeat;
import sophena.model.Producer;
import sophena.model.ProductArea;

public class ComparisonExport implements Runnable {

	private final Comparison comp;
	private final File file;

	private SheetWriter w;

	public ComparisonExport(Comparison comp, File file) {
		this.comp = comp;
		this.file = file;
	}

	@Override
	public void run() {
		if (comp == null || file == null)
			return;
		try {
			Workbook wb = new XSSFWorkbook();
			w = new SheetWriter(wb, "Ergebnisvergleich");
			overview();
			w.nextRow().nextRow();
			investments();
			w.nextRow().nextRow();
			keyFigures();
			Excel.autoSize(w.sheet, 0, comp.projects.length);

			try (var fos = new FileOutputStream(file);
					 var buffer = new BufferedOutputStream(fos)) {
				wb.write(buffer);
			}
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("Failed to export results to Excel", e);
		}
	}

	private void overview() {
		w.boldStr("Wirtschaftlichkeit")
			.nextRow()
			.nextCol();
		each(r -> w.boldStr(r.project.name));
		w.nextRow();

		w.str("Investitionskosten [EUR]");
		each(r -> w.rint(costs(r).investments));
		w.nextRow();

		w.str("Investitionsförderung [EUR]");
		each(r -> w.rint(costs(r).funding));
		w.nextRow();

		w.str("Anschlusskostenbeiträge [EUR]");
		each(r -> w.rint(r.project.costSettings.connectionFees));
		w.nextRow();

		w.boldStr("Finanzierungsbedarf [EUR]");
		each(r -> {
			var costs = costs(r);
			double cf = r.project.costSettings.connectionFees;
			w.boldRint(costs.investments - costs.funding - cf);
		});
		w.nextRow().nextRow();

		w.str("Kapitalgebundene Kosten [EUR/a]");
		each(r -> w.rint(costs(r).capitalCosts));
		w.nextRow();

		w.str("Bedarfsgebundene Kosten [EUR/a]");
		each(r -> w.rint(costs(r).consumptionCosts));
		w.nextRow();

		w.str("Betriebsgebundene Kosten [EUR/a]");
		each(r -> w.rint(costs(r).operationCosts));
		w.nextRow();

		w.str("Sonstige Kosten [EUR/a]");
		each(r -> w.rint(costs(r).otherAnnualCosts));
		w.nextRow();

		w.boldStr("Gesamtkosten [EUR/a]");
		each(r -> w.boldRint(costs(r).totalAnnualCosts));
		w.nextRow().nextRow();

		w.str("Wärmeerlöse [EUR/a]");
		each(r -> w.rint(costs(r).revenuesHeat));
		w.nextRow();

		w.str("Stromerlöse [EUR/a]");
		each(r -> w.rint(costs(r).revenuesElectricity));
		w.nextRow();

		w.boldStr("Gesamterlöse");
		each(r -> {
			var costs = costs(r);
			w.boldRint(costs.revenuesElectricity + costs.revenuesHeat);
		});
		w.nextRow().nextRow();

		w.boldStr("Jahresüberschuss [EUR/a]");
		each(r -> w.boldRint(costs(r).annualSurplus));
		w.nextRow();

		w.boldStr("Wärmegestehungskosten [EUR/MWh]");
		each(r -> w.boldRint(costs(r).heatGenerationCosts));
		w.nextRow();
	}

	private void investments() {
		var pars = new ProductAreaResult[comp.results.length];
		for (int i = 0; i < comp.results.length; i++) {
			pars[i] = ProductAreaResult.calculate(
				comp.results[i].costResultFunding);
		}

		w.boldStr("Investitionskosten");
		w.nextRow().nextCol();
		each(r -> w.boldStr(r.project.name));
		w.nextRow();

		for (ProductArea area : ProductArea.values()) {
			if (allZero(pars, area))
				continue;
			w.str(Labels.get(area) + " [EUR]");
			for (var par : pars) {
				w.rint(par.investmentCosts(area));
			}
			w.nextRow();
		}

		w.boldStr("Investitionssumme [EUR]");
		for (ProductAreaResult par : pars) {
			w.rint(par.totalInvestmentCosts);
		}
		w.nextRow();
	}

	private void keyFigures() {

		w.boldStr("Energetische Kennzahlen");
		w.nextRow().nextCol();
		each(r -> w.boldStr(r.project.name));
		w.nextRow();

		w.str("Erzeugte Wärmemenge [kWh]");
		each(r -> w.rint(r.energyResult.totalProducedHeat));
		w.nextRow();

		w.str("Installierte Leistung [kW]");
		each(r -> {
			double power = 0.0;
			for (Producer p : r.project.producers) {
				if (p.disabled)
					continue;
				power += Producers.maxPower(p);
			}
			w.rint(power);
		});
		w.nextRow();

		w.str("Trassenlänge [m]");
		each(r -> w.rint(r.project.heatNet.length));
		w.nextRow();

		w.str("Wärmebelegungsdichte [MWh/(m*a)]");
		each(r -> {
			double length = r.project.heatNet.length;
			double hl = length == 0
				? 0
				: UsedHeat.get(r) / (1000 * length);
			w.num(hl);
		});
		w.nextRow();

		w.str("Netzverluste [%]");
		each(r -> {
			var er = EfficiencyResult.calculate(r);
			double loss = 0;
			if (er.producedHeat > 0) {
				loss = 100 * er.distributionLoss / er.producedHeat;
			}
			w.rint(loss);
		});
		w.nextRow();

		w.str("CO2-Einsparung (gegen Erdgas dezentral) [kg CO2 eq./a]");
		each(r -> {
			CO2Result co2 = r.co2Result;
			w.rint(co2.variantNaturalGas - co2.total);
		});
		w.nextRow();

		w.str("Primärenergiefaktor");
		each(r -> w.num(PrimaryEnergyFactor.get(r)));
		w.nextRow();
	}

	private void each(Consumer<ProjectResult> fn) {
		for (ProjectResult r : comp.results) {
			fn.accept(r);
		}
	}

	private Summary costs(ProjectResult r) {
		return r.costResultFunding.dynamicTotal;
	}

	private boolean allZero(ProductAreaResult[] r, ProductArea area) {
		for (ProductAreaResult par : r) {
			if (par.investmentCosts(area) != 0)
				return false;
		}
		return true;
	}
}
