package sophena.io.excel.consumers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import sophena.db.Database;
import sophena.model.Consumer;
import sophena.utils.Result;
import sophena.utils.Strings;

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

	private List<ConsumerEntry> readRowsFrom(Workbook wb) {
		var entries = new ArrayList<ConsumerEntry>();
		var it = wb.sheetIterator(); it.hasNext();

		// check  each sheet, if there is an entry in the
		// top-left cell, we will read the content
		var sheet = it.next();
		var head = RowReader.of(sheet.getRow(0)).orElse(null);
		if (head == null || head.str(0, null) == null)
			return null;

		// read the consumer rows
		var nextIdx = 1;
		Row row;
		while ((row = sheet.getRow(nextIdx)) != null) {
			nextIdx++;
			var r = RowReader.of(row).orElse(null);
			if (r == null || Strings.nullOrEmpty(r.str(0, null)))
				continue;
			var entry = ConsumerEntry.readFrom(r).orElse(null);
			if (entry == null)
				continue;
			entries.add(entry);

			if (!entry.isConsumptionBased())
				continue;

			// read fuel entries
			var fuelRow = r;
			while (true) {
				var e = FuelEntry.readFrom(fuelRow).orElse(null);
				if (e == null)
					break;
				entry.add(e);
				fuelRow = RowReader.of(sheet.getRow(nextIdx)).orElse(null);
				if (isFuelContinuationRow(fuelRow)) {
					nextIdx++;
				} else {
					break;
				}
			}
		}

		return entries;
	}

	/**
	 * A consumer can have multiple fuel entries. The first entry is in the
	 * same row as the consumer definition. Additional entries are under the
	 * rows below.
	 */
	private boolean isFuelContinuationRow(RowReader r) {
		if (r == null)
			return false;
		return Strings.nullOrEmpty(r.str(Field.NAME))
				&& Strings.notEmpty(r.str(Field.FUEL));
	}
}
