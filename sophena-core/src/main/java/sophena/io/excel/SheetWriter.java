package sophena.io.excel;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

class SheetWriter {

	final Sheet sheet;
	private final CellStyle boldStyle;

	private int row = 0;
	private int col = 0;

	SheetWriter(Workbook wb, String name) {
		this.sheet = wb.createSheet(name);
		this.boldStyle = Excel.headerStyle(wb);
	}

	int incRow() {
		return ++row;
	}

	Cell boldStr(int row, int col, String val) {
		this.row = row;
		this.col = col;
		Cell cell = str(row, col, val);
		cell.setCellStyle(boldStyle);
		return cell;
	}

	Cell boldStr(int col, String val) {
		this.col = col;
		return boldStr(this.row, col, val);
	}

	Cell boldStr(String val) {
		return boldStr(this.row, ++col, val);
	}

	Cell str(int row, int col, String val) {
		this.row = row;
		this.col = col;
		return Excel.cell(sheet, row, col, val);
	}

	Cell str(int col, String val) {
		this.col = col;
		return Excel.cell(sheet, row, col, val);
	}

	Cell str(String val) {
		return Excel.cell(sheet, row, ++col, val);
	}

	Cell num(int row, int col, int val) {
		this.row = row;
		this.col = col;
		return Excel.cell(sheet, row, col, val);
	}

	Cell num(int col, int val) {
		this.col = col;
		return Excel.cell(sheet, row, col, val);
	}

	Cell num(int val) {
		return Excel.cell(sheet, row, ++col, val);
	}

	Cell num(int row, int col, double val) {
		this.row = row;
		this.col = col;
		return Excel.cell(sheet, row, col, val);
	}

	Cell num(int col, double val) {
		this.col = col;
		return Excel.cell(sheet, row, col, val);
	}

	Cell num(double val) {
		return Excel.cell(sheet, row, ++col, val);
	}

	Cell rint(int row, int col, double val) {
		this.row = row;
		this.col = col;
		return Excel.cell(sheet, row, col, Math.round(val));
	}

	Cell rint(int col, double val) {
		this.col = col;
		return Excel.cell(sheet, row, col, Math.round(val));
	}

	Cell rint(double val) {
		return Excel.cell(sheet, row, ++col, Math.round(val));
	}
}
