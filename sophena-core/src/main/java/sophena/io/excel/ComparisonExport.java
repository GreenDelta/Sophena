package sophena.io.excel;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.function.Consumer;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.calc.Comparison;
import sophena.calc.CostResult;
import sophena.calc.CostResult.FieldSet;
import sophena.calc.ProjectResult;

public class ComparisonExport implements Runnable {

	private Comparison comp;
	private File file;

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
			try (FileOutputStream fos = new FileOutputStream(file);
					BufferedOutputStream buffer = new BufferedOutputStream(
							fos)) {
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
			FieldSet costs = costs(r);
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
			FieldSet costs = costs(r);
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

	private void each(Consumer<ProjectResult> fn) {
		for (ProjectResult r : comp.results) {
			fn.accept(r);
		}
	}

	private CostResult.FieldSet costs(ProjectResult r) {
		return r.costResultFunding.dynamicTotal;
	}
}
