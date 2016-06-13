package sophena.io.excel;

import java.util.function.ToDoubleFunction;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import sophena.calc.Comparison;
import sophena.calc.CostResult;
import sophena.calc.ProjectResult;

class ComparisonSheet {

	private Comparison comparison;
	private Workbook wb;
	public int row = 0;

	ComparisonSheet(Workbook wb, Comparison comparison) {
		this.wb = wb;
		this.comparison = comparison;
	}

	void write() {
		Sheet sheet = wb.createSheet("Ergebnisvergleich");
		CellStyle style = Excel.headerStyle(wb);
		Excel.cell(sheet, row, 0,
				"Ergebnisvergleich").setCellStyle(style);
		row += 2;
		heatCosts(sheet, style);
		annualCosts(sheet, style);
		annualRevenues(sheet, style);
		investment(sheet, style);
		Excel.autoSize(sheet, 0, 2);
	}

	private void heatCosts(Sheet sheet, CellStyle style) {
		tableHead(sheet, style, "Wärmegestehungskosten (netto)", "in EUR/MWh");
		for (int i = 0; i < comparison.projects.length; i++) {
			tableBody(sheet, i, 1);
		}
		row++;
	}

	private void annualCosts(Sheet sheet, CellStyle style) {
		tableHead(sheet, style, "Jährliche Kosten (netto)", "in EUR");
		for (int i = 0; i < comparison.projects.length; i++) {
			tableBody(sheet, i, 2);
		}
		row++;
	}

	private void annualRevenues(Sheet sheet, CellStyle style) {
		ToDoubleFunction<CostResult> fn = result -> {
			if (result == null || result.netTotal == null)
				return 0;
			return result.netTotal.revenues;
		};
		double max = 0;
		for (ProjectResult result : comparison.results) {
			max = Math.max(max, fn.applyAsDouble(result.costResult));
			max = Math.max(max, fn.applyAsDouble(result.costResultFunding));
		}
		if (max == 0)
			return;
		tableHead(sheet, style, "Jährliche Erlöse (netto)", "in EUR");
		for (int i = 0; i < comparison.projects.length; i++) {
			tableBody(sheet, i, 3);
		}
		row++;
	}

	private void investment(Sheet sheet, CellStyle style) {
		tableHead(sheet, style, "Investitionskosten (netto)", "in EUR");
		for (int i = 0; i < comparison.projects.length; i++) {
			tableBody(sheet, i, 4);
		}
		row++;
	}

	private void tableHead(Sheet sheet, CellStyle style, String title, String unit) {
		Excel.cell(sheet, row, 0, title).setCellStyle(style);
		Excel.cell(sheet, row, 1, "Ohne Förderung").setCellStyle(style);
		Excel.cell(sheet, row, 2, "Mit Förderung").setCellStyle(style);
		row++;
		Excel.cell(sheet, row, 0, "Projekt").setCellStyle(style);
		Excel.cell(sheet, row, 1, unit);
		Excel.cell(sheet, row, 2, unit);
		row++;
	}

	private void tableBody(Sheet sheet, int i, int type) {
		String project = comparison.projects[i].name;
		CostResult res = comparison.results[i].costResult;
		CostResult resFund = comparison.results[i].costResultFunding;
		Excel.cell(sheet, row, 0, project);
		switch (type) {
		case 1:
			Excel.cell(sheet, row, 1, getHeatCosts(res));
			Excel.cell(sheet, row, 2, getHeatCosts(resFund));
			break;
		case 2:
			Excel.cell(sheet, row, 1, getAnnualCosts(res));
			Excel.cell(sheet, row, 2, getAnnualCosts(resFund));
			break;
		case 3:
			Excel.cell(sheet, row, 1, getAnnualRevenues(res));
			Excel.cell(sheet, row, 2, getAnnualRevenues(resFund));
			break;
		case 4:
			Excel.cell(sheet, row, 1, getInvestment(res));
			Excel.cell(sheet, row, 2, getInvestment(resFund));
			break;
		default:
			break;
		}
		row++;
	}

	private double getHeatCosts(CostResult res) {
		if (res == null || res.netTotal == null)
			return 0;
		double val = res.netTotal.heatGenerationCosts;
		return Math.round(val * 1000);
	}

	private double getAnnualCosts(CostResult res) {
		if (res == null || res.netTotal == null)
			return 0;
		return Math.round(res.netTotal.annualCosts);
	}

	private double getAnnualRevenues(CostResult res) {
		if (res == null || res.netTotal == null)
			return 0;
		return Math.round(res.netTotal.revenues);
	}

	private double getInvestment(CostResult res) {
		if (res == null || res.netTotal == null)
			return 0;
		return Math.round(res.netTotal.investments);
	}
}
