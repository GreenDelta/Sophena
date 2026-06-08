package sophena.rcp.editors.biogas.electricity;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import sophena.io.excel.Excel;
import sophena.model.Stats;
import sophena.model.biogas.ElectricityPriceCurve;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.MsgBox;

class ElectricityPriceIO {

	static void write(ElectricityPriceCurve curve) {
		if (curve == null || curve.values == null)
			return;
		var name = curve.name != null
			? curve.name + ".xlsx"
			: "Strompreise.xlsx";
		var file = FileChooser.save(name, "xlsx");
		if (file == null)
			return;

		try (var wb = WorkbookFactory.create(true);
				 var out = new FileOutputStream(file)) {
			var sheet = wb.createSheet("Strompreise");
			var hs = Excel.headerStyle(wb);
			Excel.cell(sheet, 0, 0, "Datum").setCellStyle(hs);
			Excel.cell(sheet, 0, 1, "von").setCellStyle(hs);
			Excel.cell(sheet, 0, 2, "bis").setCellStyle(hs);
			Excel.cell(sheet, 0, 3, "Spotmarktpreis in ct/kWh").setCellStyle(hs);
			Excel.cell(sheet, 0, 4, "Einspeisung erlaubt").setCellStyle(hs);

			var values = curve.values;
			var feedIn = curve.feedInAllowed;

			int year = LocalDate.now().getYear();
			if (Year.isLeap(year)) {
				year--;
			}
			var start = LocalDateTime.of(year, 1, 1, 0, 0);
			var dateFormatter = DateTimeFormatter.ofPattern("dd.MM.");
			var timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

			for (int i = 0; i < values.length; i++) {
				var time = start.plusHours(i);
				var dateStr = time.format(dateFormatter);
				var vonStr = time.format(timeFormatter);
				var bisStr = time.plusHours(1).format(timeFormatter);

				var rowIdx = i + 1;
				Excel.cell(sheet, rowIdx, 0, dateStr);
				Excel.cell(sheet, rowIdx, 1, vonStr);
				Excel.cell(sheet, rowIdx, 2, bisStr);
				Excel.cell(sheet, rowIdx, 3, values[i]);

				int allowed = (feedIn == null || i >= feedIn.length || feedIn[i]) ? 1 : 0;
				Excel.cell(sheet, rowIdx, 4, allowed);
			}
			wb.write(out);
		} catch (Exception e) {
			MsgBox.error("Fehler beim Schreiben der Strompreise: " + e.getMessage());
		}
	}

	static boolean read(File file, ElectricityPriceCurve curve) {
		if (file == null || !file.exists())
			return false;
		try (var wb = WorkbookFactory.create(file)) {
			var sheet = wb.getSheetAt(0);
			var values = new double[Stats.HOURS];
			var feedInAllowed = new boolean[Stats.HOURS];
			for (int i = 0; i < Stats.HOURS; i++) {
				values[i] = Excel.getDouble(sheet, i + 1, 3);
				feedInAllowed[i] = isFeedInAllowed(sheet, i + 1);
			}
			curve.values = values;
			curve.feedInAllowed = feedInAllowed;
			return true;
		} catch (Exception e) {
			MsgBox.error("Fehler beim Lesen der Strompreise: " + e.getMessage());
			return false;
		}
	}

	private static boolean isFeedInAllowed(Sheet sheet, int rowIdx) {
		var row = sheet.getRow(rowIdx);
		if (row == null) return true;
		var cell = row.getCell(4);
		if (cell == null) return true;

		try {
			return switch (cell.getCellType()) {
				case NUMERIC -> cell.getNumericCellValue() != 0.0;
				case BOOLEAN -> cell.getBooleanCellValue();
				case STRING -> {
					var str = cell.getStringCellValue();
					if (str == null) yield true;
					str = str.trim().toLowerCase(Locale.ROOT);
					yield !("0".equals(str) || str.startsWith("f"));
				}
				default -> true;
			};
		} catch (Exception e) {
			return true;
		}
	}
}
