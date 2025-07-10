package sophena.rcp.editors.biogas.electricity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Optional;

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
				? curve.name + " - Strompreise.xlsx"
				: "Strompreise.xlsx";
		var file = FileChooser.save(name, "xlsx");
		if (file == null)
			return;

		try (var wb = WorkbookFactory.create(true);
				 var out = new FileOutputStream(file)) {
			var sheet = wb.createSheet("Strompreise");
			var hs = Excel.headerStyle(wb);
			Excel.cell(sheet, 0, 0, "Stunde").setCellStyle(hs);
			Excel.cell(sheet, 0, 1, "Preis [ct/kWh]").setCellStyle(hs);

			var values = curve.values;
			for (int i = 0; i < values.length; i++) {
				var h = i + 1;
				Excel.cell(sheet, h, 0, h);
				Excel.cell(sheet, h, 1, values[i]);
			}
			wb.write(out);
		} catch (Exception e) {
			MsgBox.error("Fehler beim Schreiben der Strompreise: " + e.getMessage());
		}
	}

	static Optional<double[]> read(File file) {
		if (file == null || !file.exists())
			return Optional.empty();
		try (var wb = WorkbookFactory.create(file)) {
			var sheet = wb.getSheetAt(0);
			var values = new double[Stats.HOURS];
			for (int i = 0; i < Stats.HOURS; i++) {
				values[i] = Excel.getDouble(sheet, i+1, 1);
			}
			return Optional.of(values);
		} catch (Exception e) {
			MsgBox.error("Fehler beim Lesen der Strompreise: " + e.getMessage());
			return Optional.empty();
		}
	}
}
