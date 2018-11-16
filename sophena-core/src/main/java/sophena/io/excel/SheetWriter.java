package sophena.io.excel;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * A cursor based sheet writer that simplifies the Excel export a bit.
 */
class SheetWriter {

	final Sheet sheet;
	private final CellStyle bold;

	// the current cursor position.
	private int row = 0;
	private int col = 0;

	SheetWriter(Workbook wb, String name) {
		this.sheet = wb.createSheet(name);
		this.bold = Excel.headerStyle(wb);
	}

	/** Moves the cursor to the next row and the first column of that row. */
	SheetWriter nextRow() {
		row++;
		col = 0;
		return this;
	}

	/** Moves the cursor to the next column of the current row. */
	SheetWriter nextCol() {
		col++;
		return this;
	}

	// boldStr::String

	/**
	 * Writes the given string in bold format and then moves the cursor to the
	 * next column of the current row.
	 */
	SheetWriter boldStr(String val) {
		Excel.cell(sheet, row, col, val).setCellStyle(bold);
		col++;
		return this;
	}

	/**
	 * Moves the cursor to the given column of the current row and writes the
	 * given string in bold format.
	 */
	SheetWriter boldStr(int col, String val) {
		this.col = col;
		return boldStr(val);
	}

	/**
	 * Moves the cursor to the given position and writes the given string in
	 * bold format.
	 */
	SheetWriter boldStr(int row, int col, String val) {
		this.row = row;
		this.col = col;
		return boldStr(val);
	}

	// str::String

	/**
	 * Writes the given string and then moves the cursor to the next column of
	 * the current row.
	 */
	SheetWriter str(String val) {
		Excel.cell(sheet, row, col, val);
		col++;
		return this;
	}

	/**
	 * Moves the cursor to the given column of the current row and writes the
	 * given string.
	 */
	SheetWriter str(int col, String val) {
		this.col = col;
		return str(val);
	}

	/**
	 * Moves the cursor to the given position and writes the given string.
	 */
	SheetWriter str(int row, int col, String val) {
		this.row = row;
		this.col = col;
		return str(val);
	}

	// num::int

	/**
	 * Writes the given integer and then moves the cursor to the next column of
	 * the current row.
	 */
	SheetWriter num(int val) {
		Excel.cell(sheet, row, col, val);
		col++;
		return this;
	}

	/**
	 * Moves the cursor to the given column of the current row and writes the
	 * given integer.
	 */
	SheetWriter num(int col, int val) {
		this.col = col;
		return num(val);
	}

	/**
	 * Moves the cursor to the given position and writes the given integer.
	 */
	SheetWriter num(int row, int col, int val) {
		this.row = row;
		this.col = col;
		return num(val);
	}

	// num::double

	/**
	 * Writes the given number and then moves the cursor to the next column of
	 * the current row.
	 */
	SheetWriter num(double val) {
		Excel.cell(sheet, row, col, val);
		col++;
		return this;
	}

	/**
	 * Moves the cursor to the given column of the current row and writes the
	 * given number.
	 */
	SheetWriter num(int col, double val) {
		this.col = col;
		return num(val);
	}

	/**
	 * Moves the cursor to the given position and writes the given number.
	 */
	SheetWriter num(int row, int col, double val) {
		this.row = row;
		this.col = col;
		return num(val);
	}

	// rint::double

	/**
	 * Writes the given number as integer and then moves the cursor to the next
	 * column of the current row.
	 */
	SheetWriter rint(double val) {
		Excel.cell(sheet, row, col, Math.round(val));
		col++;
		return this;
	}

	/**
	 * Moves the cursor to the given column of the current row and writes the
	 * given number as integer.
	 */
	SheetWriter rint(int col, double val) {
		this.col = col;
		return rint(val);
	}

	/**
	 * Moves the cursor to the given position and writes the given number as
	 * integer.
	 */
	SheetWriter rint(int row, int col, double val) {
		this.row = row;
		this.col = col;
		return rint(val);
	}

	// boldRint::double

	/**
	 * Writes the given number as integer in bold format and then moves the
	 * cursor to the next column of the current row.
	 */
	SheetWriter boldRint(double val) {
		Excel.cell(sheet, row, col, Math.round(val)).setCellStyle(bold);
		col++;
		return this;
	}

	/**
	 * Moves the cursor to the given column of the current row and writes the
	 * given number as integer in bold format.
	 */
	SheetWriter boldRint(int col, double val) {
		this.col = col;
		return boldRint(val);
	}

	/**
	 * Moves the cursor to the given position and writes the given number as
	 * integer in bold format.
	 */
	SheetWriter boldRint(int row, int col, double val) {
		this.row = row;
		this.col = col;
		return boldRint(val);
	}

}
