package sophena.io.excel;

import java.util.Arrays;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import sophena.calc.ProjectLoad;
import sophena.calc.ProjectResult;
import sophena.math.energetic.FuelDemand;
import sophena.math.energetic.FullLoadHours;
import sophena.math.energetic.Producers;
import sophena.math.energetic.UtilisationRate;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.Project;
import sophena.model.Stats;
import sophena.rcp.Labels;

class HeatSheet {

	private Workbook wb;
	private ProjectResult result;
	private Project project;

	HeatSheet(Workbook wb, ProjectResult result, Project project) {
		this.wb = wb;
		this.result = result;
		this.project = project;
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
			Excel.cell(sheet, row, 3, Math.round(pr.boiler.maxPower));
			Excel.cell(sheet, row, 4, Math.round(heat));
			double share = Math.round(100 * result.energyResult.totalHeat(pr)
					/ load);
			Excel.cell(sheet, row, 5, Math.round(share > 100 ? 100 : share));
			Excel.cell(sheet, row, 6, Math.round(FullLoadHours.get(pr, heat)));
			Excel.cell(sheet, row, 7, Math.round(UtilisationRate.get(pr,
					result.energyResult) * 100));
			row++;
		}
		powerDiffRow(sheet, row, result.energyResult.producers);
		Excel.autoSize(sheet, 0, 7);
	}

	private String getFuelUse(Producer pr, double heat) {
		return Labels.getFuel(pr) + ": "
				+ (int) FuelDemand.getAmount(pr, result.energyResult)
				+ " " + Labels.getFuelUnit(pr);
	}

	private void powerDiffRow(Sheet sheet, int row, Producer[] producers) {
		double diff = calculateDiff(producers);
		double powerDiff = calculatePowerDiff(producers, project);
		if (diff != 0 || powerDiff < 0) {
			Excel.cell(sheet, row, 0, "Ungedeckte Leistung");
			Excel.cell(sheet, row, 3, Math.round(powerDiff));
			Excel.cell(sheet, row, 4, Math.round(diff));
			double shareDiff = Math.round(100 * diff
					/ result.energyResult.totalLoad);
			Excel.cell(sheet, row, 5, Math.round((shareDiff > 100 ? 100 : shareDiff)));
			row++;
		}
		Excel.cell(sheet, row, 0, "Pufferspeicher");
		Excel.cell(sheet, row, 4,
				Math.round(result.energyResult.totalBufferedHeat));
		double shareBuff = Math.round(100
				* result.energyResult.totalBufferedHeat
				/ result.energyResult.totalLoad);
		Excel.cell(sheet, row, 5, Math.round((shareBuff > 100 ? 100 : shareBuff)));
	}

	private double calculateDiff(Producer[] producers) {
		double diff = 0;
		for (int i = 0; i < Stats.HOURS; i++) {
			double supplied = result.energyResult.suppliedPower[i];
			double load = result.energyResult.loadCurve[i];
			if (supplied < load)
				diff += (load - supplied);
		}
		return diff;
	}

	private double calculatePowerDiff(Producer[] producers, Project p) {
		double maxLoad;
		maxLoad = ProjectLoad.getMax(p);
		if (p.heatNet != null)
			maxLoad = Math.ceil(maxLoad * p.heatNet.simultaneityFactor);

		double power = 0;
		for (Producer producer : producers) {
			power += Producers.maxPower(producer);
		}
		return power - maxLoad;
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
