package sophena.io.excel.consumers;

import java.text.DecimalFormat;
import java.util.Objects;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Row;

class RowReader {

	private final Row row;

	private RowReader(Row row) {
		this.row = Objects.requireNonNull(row);
	}

	static Optional<RowReader> of(Row row) {
		return row != null
				? Optional.of(new RowReader(row))
				: Optional.empty();
	}

	int index() {
		return row.getRowNum();
	}

	String str(Field field) {
		return field != null
				? str(field.column, null)
				: null;
	}
	
	String str(Field field, DecimalFormat df) {
		return field != null
				? str(field.column, df)
				: null;
	}

	String str(int pos, DecimalFormat df) {
		var s = rawStr(pos, df);
		if (s == null)
			return null;
		var trimmed = s.strip();
		return trimmed.isEmpty()
				? null
				: trimmed;
	}

	private String rawStr(int pos, DecimalFormat df) {
		var cell = row.getCell(pos);
		if (cell == null)
			return null;
		return switch (cell.getCellType()) {
			case STRING -> cell.getStringCellValue();
			case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
			case FORMULA -> cell.getCellFormula();
			case NUMERIC -> df != null 
					? df.format(cell.getNumericCellValue()) 
					: Double.toString(cell.getNumericCellValue());
			default -> null;
		};
	}

	Double num(Field field) {
		return field != null
				? num(field.column)
				: null;
	}

	private Double num(int pos) {
		var cell = row.getCell(pos);
		if (cell == null)
			return null;
		try {
			return cell.getNumericCellValue();
		} catch (Exception e) {
			return null;
		}
	}
}
