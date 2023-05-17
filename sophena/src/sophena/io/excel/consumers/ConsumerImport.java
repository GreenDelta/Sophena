package sophena.io.excel.consumers;

import org.apache.poi.ss.usermodel.WorkbookFactory;
import sophena.db.Database;

import java.io.File;

public class ConsumerImport {

	private final File file;
	private final Database db;

	private ConsumerImport(File file, Database db) {
		this.file = file;
		this.db = db;
	}

	public static ConsumerImport of(File file, Database db) {
		return new ConsumerImport(file, db);
	}

	public String run() {
		try (var wb = WorkbookFactory.create(file)){
			for (var it = wb.sheetIterator(); it.hasNext();) {
				var sheet = it.next();
				var head = RowReader.of(sheet.getRow(0)).orElse(null);
				if (head == null || head.str(0) == null)
					continue;

				for (var rowIt = sheet.rowIterator(); rowIt.hasNext(); ) {
					var row = rowIt.next();
					if (row.getRowNum() == 0)
						continue;
				}
			}
			return null;
		} catch (Exception e) {
			return "Die Datei konnte nicht gelesen werden: " + file.getName();
		}
	}
}
