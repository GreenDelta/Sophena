package sophena.rcp.editors.biogas.plant;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Optional;

import org.apache.poi.ss.usermodel.WorkbookFactory;

import sophena.io.excel.Excel;
import sophena.model.Stats;
import sophena.model.biogas.SubstrateProfile;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.MsgBox;

class SubstrateProfileIO {

	static void write(SubstrateProfile profile) {
		if (profile == null || profile.hourlyValues == null)
			return;
		var name = profile.substrate != null
				? profile.substrate.name + " - Verteilung.xlsx"
				: "Substratverteilung.xlsx";
		var file = FileChooser.save(name, "xlsx");
		if (file == null)
			return;

		try (var wb = WorkbookFactory.create(true);
				 var out = new FileOutputStream(file)) {
			var sheet = wb.createSheet("Substratprofil");
			var hs = Excel.headerStyle(wb);
			Excel.cell(sheet, 0, 0, "Stunde").setCellStyle(hs);
			Excel.cell(sheet, 0, 1, "Masse [t]").setCellStyle(hs);

			var vs = profile.hourlyValues;
			for (int i = 0; i < vs.length; i++) {
				var h = i + 1;
				Excel.cell(sheet, h, 0, h);
				Excel.cell(sheet, h, 1, vs[i]);
			}
			wb.write(out);
		} catch (Exception e) {
			MsgBox.error("Fehler beim Schreiben der Profildaten: " + e.getMessage());
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
			MsgBox.error("Fehler beim Lesen der Profildaten: " + e.getMessage());
			return Optional.empty();
		}
	}
}
