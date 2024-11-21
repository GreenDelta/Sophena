package sophena.io.excel.consumers;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import sophena.db.Database;
import sophena.model.Consumer;
import sophena.utils.Result;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ConsumerReader {

	private final File file;
	private final Database db;

	private ConsumerReader(File file, Database db) {
		this.file = file;
		this.db = db;
	}

	public static ConsumerReader of(File file, Database db) {
		return new ConsumerReader(file, db);
	}

	public Result<List<Consumer>> read() {
		try (var wb = WorkbookFactory.create(file)) {
			var rows = readRowsFrom(wb);
			if (rows.isEmpty())
				return Result.ok(List.of());
			var consumers = new ArrayList<Consumer>();
			for (var row : rows) {
				var r = row.toConsumer(db);
				if (r.isError())
					return Result.error(r.message().orElse("unbekannter Fehler"));
				consumers.add(r.get());
			}
			return Result.ok(consumers);
		} catch (Exception e) {
			return Result.error(
					"Die Datei konnte nicht gelesen werden: " + e.getMessage());
		}
	}

	private List<ConsumerRow> readRowsFrom(Workbook wb) {
		var consumers = new ArrayList<ConsumerRow>();
		var it = wb.sheetIterator(); 
		var sheet = it.next();
		var head = RowReader.of(sheet.getRow(0)).orElse(null);
		if (head == null || head.str(0, null) == null)
			return null;
		for (var rowIt = sheet.rowIterator(); rowIt.hasNext(); ) {
			var row = rowIt.next();
			if (row.getRowNum() == 0)
				continue;
			var r = RowReader.of(row).orElse(null);
			if (r == null)
				continue;
			ConsumerRow.readFrom(r).ifPresent(consumers::add);
		}		
		return consumers;
	}


}
