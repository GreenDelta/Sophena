package sophena.io;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import sophena.model.LoadProfile;
import sophena.model.Stats;
import sophena.utils.Result;
import sophena.utils.Strings;

public class LoadProfileReader {

	public Result<LoadProfile> read(File file) {
		try {
			// read all lines
			var rows = Files.readAllLines(file.toPath());
			if (rows.size() < 2)
				return Result.error("Die Datei hat weniger als zwei Zeilen.");

			// determine the column separator
			var sep = getSeparator(rows);
			if ("?".equals(sep))
				return Result.error(
						"Das Spaltentrennzeichen konnte nicht ermittelt werden");

			// check the number of rows
			String warning = null;
			if (rows.size() != (Stats.HOURS + 1)) {
				warning = "Die Datei enthält weniger als "
						+ (Stats.HOURS + 1) + "Zeilen";
			}

			// parse the rows
			var profile = LoadProfile.initEmpty();
			for (int row = 1; row < rows.size(); row++) {
				var line = rows.get(row);
				if (Strings.nullOrEmpty(line))
					continue;

				// split the row and check the format
				var parts = line.split(sep);
				if (parts.length < 2)
					return Result.error("Ungültiges Dateiformat: Die Zeile "
							+ (row + 1) + " hat weniger als 2 Spalten");
				if (parts.length < 3) {
					warning = "Es fehlen Werte in der dritten Spalte";
				}

				// parse the numbers
				try {
					int idx = Integer.parseInt(parts[0], 10) - 1;
					double dyn = Double.parseDouble(parts[1].replace(',', '.'));
					double stat = parts.length > 2
							? Double.parseDouble(parts[2].replace(',', '.'))
							: 0.0;

					if (idx < 0 || idx > Stats.HOURS)
						return Result.error("Ungültige Stunde in Zeile "
								+ (row + 1) + ": " + (idx + 1));
					profile.dynamicData[idx] = dyn;
					profile.staticData[idx] = stat;
				} catch (Exception e) {
					return Result.error("Die Zahlen in Zeile "
							+ (row + 1) + " konnten nicht gelesen werden.");
				}
			} // for

			return warning != null
					? Result.warning(profile, warning)
					: Result.ok(profile);

		} catch (Exception e) {
			return Result.error(
					"Die Datei konnte nicht gelesen werden: " + e.getMessage());
		}
	}

	/**
	 * Identify the column separator from the content of the file.
	 */
	private String getSeparator(List<String> rows) {
		var header = rows.get(0);
		boolean inQuotes = false;
		boolean hasSemicolon = false;
		boolean hasComma = false;
		for (char c : header.toCharArray()) {
			if (c == '"') {
				inQuotes = !inQuotes;
				continue;
			}
			if (inQuotes)
				continue;
			switch (c) {
				case ';':
					hasSemicolon = true;
					break;
				case ',':
					hasComma = true;
					break;
				default:
					break;
			}
		}
		return hasSemicolon
				? ";"
				: hasComma ? "," : "?";
	}
}
