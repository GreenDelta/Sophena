package sophena.io.excel;

import java.util.Arrays;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import sophena.calc.ProjectResult;
import sophena.math.energetic.FuelDemand;
import sophena.math.energetic.FullLoadHours;
import sophena.math.energetic.UtilisationRate;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.Stats;
import sophena.rcp.Labels;

class HeatSheet {

	private Workbook wb;
	private ProjectResult result;

	HeatSheet(Workbook wb, ProjectResult result) {
		this.wb = wb;
		this.result = result;
	}

	void write() {
		Sheet sheet = wb.createSheet("Wärme");
		header(sheet);
		int row = 1;
		Arrays.sort(result.energyResult.producers,
				(r1, r2) -> Integer.compare(r1.rank, r2.rank));
		for (Producer pr : result.energyResult.producers) {
			double heat = result.energyResult.totalHeat(pr);
			double load = result.energyResult.totalLoad;
			Excel.cell(sheet, row, 0, pr.name);
			if (pr.function == ProducerFunction.BASE_LOAD)
				Excel.cell(sheet, row, 1, pr.rank + " - Grundlast");
			else
				Excel.cell(sheet, row, 1, pr.rank + " - Spitzenlast");
			Excel.cell(sheet, row, 2, getFuelUse(pr, heat));
			Excel.cell(sheet, row, 3, (int) pr.boiler.maxPower);
			Excel.cell(sheet, row, 4, (int) heat);
			double share = Math.round(100 * result.energyResult.totalHeat(pr)
					/ load);
			Excel.cell(sheet, row, 5, (int) (share > 100 ? 100 : share));
			Excel.cell(sheet, row, 6, (int) FullLoadHours.get(pr, heat));
			Excel.cell(sheet, row, 7, (int) UtilisationRate.get(pr,
					result.energyResult));
			row++;
		}
		powerDiffRow(sheet, row, result.energyResult.producers);
		Excel.autoSize(sheet, 0, 1);
	}

	private String getFuelUse(Producer pr, double heat) {
		return Labels.getFuel(pr) + ": "
				+ (int) FuelDemand.getAmount(pr, result.energyResult)
				+ " " + Labels.getFuelUnit(pr);
	}

	private void powerDiffRow(Sheet sheet, int row, Producer[] producers) {
		double diff = calculatePowerDiff(producers);
		if (diff != 0) {
			Excel.cell(sheet, row, 0, "Ungedeckte Leistung");
			Excel.cell(sheet, row, 4, (int) diff);
			double shareDiff = Math.round(100 * diff
					/ result.energyResult.totalLoad);
			Excel.cell(sheet, row, 5, (int) (shareDiff > 100 ? 100 : shareDiff));
			row++;
		}
		Excel.cell(sheet, row, 0, "Pufferspeicher");
		Excel.cell(sheet, row, 4,
				(int) result.energyResult.totalBufferedHeat);
		double shareBuff = Math.round(100
				* result.energyResult.totalBufferedHeat
				/ result.energyResult.totalLoad);
		Excel.cell(sheet, row, 5, (int) (shareBuff > 100 ? 100 : shareBuff));
	}

	private double calculatePowerDiff(Producer[] producers) {
		double diff = 0;
		for (int i = 0; i < Stats.HOURS; i++) {
			double supplied = result.energyResult.suppliedPower[i];
			double load = result.energyResult.loadCurve[i];
			if (supplied < load)
				diff += (load - supplied);
		}
		return diff;
	}

	private void header(Sheet sheet) {
		CellStyle style = Excel.headerStyle(wb);
		Excel.cell(sheet, 0, 0, "Wärmeerzeuger").setCellStyle(style);
		Excel.cell(sheet, 0, 1, "Rang").setCellStyle(style);
		Excel.cell(sheet, 0, 2, "Brennstoffverbrauch in m3").setCellStyle(style);
		Excel.cell(sheet, 0, 3, "Nennleistung in kW").setCellStyle(style);
		Excel.cell(sheet, 0, 4, "Erzeugte Wärme in kWh").setCellStyle(style);
		Excel.cell(sheet, 0, 5, "Anteil in %").setCellStyle(style);
		Excel.cell(sheet, 0, 6, "Volllaststunden in h").setCellStyle(style);
		Excel.cell(sheet, 0, 7, "Nutzungsgrad in %").setCellStyle(style);
	}
}
