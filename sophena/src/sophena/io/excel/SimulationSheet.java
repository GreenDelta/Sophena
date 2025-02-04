package sophena.io.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import sophena.calc.EnergyResult;
import sophena.model.Producer;

class SimulationSheet {

	private Workbook wb;
	private EnergyResult result;

	SimulationSheet(Workbook wb, EnergyResult result) {
		this.wb = wb;
		this.result = result;
	}

	void write() {
		if (result == null)
			return;
		Sheet sheet = wb.createSheet("Simulationsergebnisse");
		header(sheet);
		for (int r = 1; r <= 8760; r++) {
			Row row = sheet.createRow(r);
			int i = r - 1;
			double diff = result.suppliedPower[i] - result.loadCurve[i];
			Excel.cell(sheet, r, 0, r);
			cell(row, 0, r);
			cell(row, 1, round(result.loadCurve[i]));
			cell(row, 2, round(result.suppliedPower[i] + result.suppliedBufferHeat[i]));
			cell(row, 3, round(diff));
			cell(row, 4, round(result.bufferCapacity[i]));
			cell(row, 5, round(result.suppliedBufferHeat[i]));
			cell(row, 6).setCellValue(round(result.bufferLoss[i] * 10) / 10d);
			for (int k = 0; k < result.producers.length; k++) {
				cell(row, 7 + k, round(result.producerResults[k][i]));
			}
		}
		for (int i = 0; i < (7 + result.producers.length); i++) {
			sheet.setColumnWidth(i, 25 * 256);
		}
	}

	private void header(Sheet sheet) {
		CellStyle style = Excel.headerStyle(wb);
		String[] columns = {
				"Stunde",
				"Benötigte Leistung",
				"Gelieferte Leistung",
				"Differenz",
				"Pufferspeicherkapazität",
				"Beitrag Pufferspeicher",
				"Pufferspeicherverluste" };
		int col = 0;
		for (String s : columns) {
			Excel.cell(sheet, 0, col, s).setCellStyle(style);
			col++;
		}
		for (Producer p : result.producers) {
			Excel.cell(sheet, 0, col, p.name).setCellStyle(style);
			col++;
		}
	}

	private int round(double number) {
		return (int) Math.round(number);
	}

	private Cell cell(Row row, int column, int value) {
		Cell c = cell(row, column);
		c.setCellValue(value);
		return c;
	}

	private Cell cell(Row row, int column) {
		return row.createCell(column);
	}
}
