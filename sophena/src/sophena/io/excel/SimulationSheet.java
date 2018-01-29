package sophena.io.excel;

import org.apache.poi.ss.usermodel.CellStyle;
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
		for (int row = 1; row <= 8760; row++) {
			int i = row - 1;
			double diff = result.suppliedPower[i] - result.loadCurve[i];
			Excel.cell(sheet, row, 0, row);
			Excel.cell(sheet, row, 1, r(result.loadCurve[i]));
			Excel.cell(sheet, row, 2, r(result.suppliedPower[i]));
			Excel.cell(sheet, row, 3, r(diff));
			Excel.cell(sheet, row, 4, r(result.bufferCapacity[i]));
			Excel.cell(sheet, row, 5, r(result.suppliedBufferHeat[i]));
			Excel.cell(sheet, row, 6, r(result.bufferLoss[i]));
			for (int k = 0; k < result.producers.length; k++) {
				Excel.cell(sheet, row, 7 + k, r(result.producerResults[k][i]));
			}
		}
		Excel.autoSize(sheet, 0, 6);
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

	/** Rounds the given number and converts it to an interger number. */
	private int r(double number) {
		return (int) Math.round(number);
	}

}
