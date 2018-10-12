package sophena.io.excel;

import java.util.Arrays;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import sophena.calc.ProjectResult;
import sophena.math.energetic.GeneratedElectricity;
import sophena.math.energetic.Producers;
import sophena.model.Producer;
import sophena.model.ProducerFunction;

class ElectricitySheet {

	private Workbook wb;
	private ProjectResult result;

	ElectricitySheet(Workbook wb, ProjectResult result) {
		this.wb = wb;
		this.result = result;
	}

	void write() {
		Sheet sheet = wb.createSheet("Strom");
		header(sheet);
		int row = 1;
		Arrays.sort(result.energyResult.producers,
				(r1, r2) -> Integer.compare(r1.rank, r2.rank));
		double total = calculateTotal(result.energyResult.producers);
		for (Producer p : result.energyResult.producers) {
			if (p.boiler == null || !p.boiler.isCoGenPlant)
				continue;
			double heat = result.energyResult.totalHeat(p);
			double value = GeneratedElectricity.get(p, result);
			Excel.cell(sheet, row, 0, p.name);
			if (p.function == ProducerFunction.BASE_LOAD) {
				Excel.cell(sheet, row, 1, p.rank + " - Grundlast");
			} else {
				Excel.cell(sheet, row, 1, p.rank + " - Spitzenlast");
			}
			Excel.cell(sheet, row, 2, Math.round(p.boiler.maxPowerElectric));
			Excel.cell(sheet, row, 3, Math.round(value));
			Excel.cell(sheet, row, 4, Math.round(((value / total) * 100)));
			Excel.cell(sheet, row, 5,
					Math.round(Producers.fullLoadHours(p, heat)));
			Excel.cell(sheet, row, 6,
					Math.round(p.boiler.efficiencyRateElectric * 100d));
			row++;
		}
		Excel.autoSize(sheet, 0, 1);
	}

	private double calculateTotal(Producer[] producers) {
		double total = 0d;
		for (Producer pr : producers) {
			total += GeneratedElectricity.get(pr, result);
		}
		return total;
	}

	private void header(Sheet sheet) {
		CellStyle style = Excel.headerStyle(wb);
		Excel.cell(sheet, 0, 0, "Wärmeerzeuger").setCellStyle(style);
		Excel.cell(sheet, 0, 1, "Rang").setCellStyle(style);
		Excel.cell(sheet, 0, 2, "Nennleistung in kW").setCellStyle(style);
		Excel.cell(sheet, 0, 3, "Erzeugter Strom in kWh").setCellStyle(style);
		Excel.cell(sheet, 0, 4, "Anteil in %").setCellStyle(style);
		Excel.cell(sheet, 0, 5, "Volllaststunden in h").setCellStyle(style);
		Excel.cell(sheet, 0, 6, "Wirkungsgrad in %").setCellStyle(style);
	}

}
