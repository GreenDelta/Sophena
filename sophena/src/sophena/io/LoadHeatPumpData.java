package sophena.io;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import sophena.model.ProducerProfile;
import sophena.model.Stats;
import sophena.rcp.editors.basedata.heatpumps.HeatPumpWizard.HeatPumpData;
import sophena.rcp.utils.Texts;
import sophena.utils.Result;
import sophena.utils.Strings;

public class LoadHeatPumpData {
	
	public static Result<List<HeatPumpData>> readHeatPumpData(File file) {
		try {
			// read all lines
			var rows = Files.readAllLines(file.toPath());
			if (rows.size() < 2)
				return Result.error("Die Datei hat weniger als zwei Zeilen.");

			// determine the column separator
			var sep = LoadProfiles.getSeparator(rows);
			if ("?".equals(sep))
				return Result.error(
						"Das Spaltentrennzeichen konnte nicht ermittelt werden");

			String warning = null;

			// parse the rows
			List<HeatPumpData> list = new ArrayList<>();
			for (int row = 1; row < rows.size(); row++) {
				var line = rows.get(row);
				if (Strings.nullOrEmpty(line))
					continue;

				// split the row and check the format
				var parts = line.split(sep);
				if (parts.length < 4)
					return Result.error("Ungültiges Dateiformat: Die Zeile "
							+ (row + 1) + " hat weniger als 4 Spalten");

				// parse the numbers
				try {
					double targettemp = 5*(Math.round((int)Double.parseDouble(parts[0].replace(',', '.'))/5));
					double sourcetemp = 5*(Math.round((int)Double.parseDouble(parts[1].replace(',', '.'))/5));
					double maxPower = Double.parseDouble(parts[2].replace(',', '.'));
					double cop = Double.parseDouble(parts[3].replace(',', '.'));;

					var heatPumpData = new HeatPumpData(targettemp, sourcetemp, maxPower, cop);
					list.add(heatPumpData);
				} catch (Exception e) {
					return Result.error("Die Zahlen in Zeile "
							+ (row + 1) + " konnten nicht gelesen werden.");
				}
			}

			return warning != null
					? Result.warning(list, warning)
					: Result.ok(list);

		} catch (Exception e) {
			return Result.error(
					"Die Datei konnte nicht gelesen werden: " + e.getMessage());
		}
	}
	
	public static Result<double[]> readHourlyTemperature(File file) {
		try {
			// read all lines
			var rows = Files.readAllLines(file.toPath());
			if (rows.size() < 2)
				return Result.error("Die Datei hat weniger als zwei Zeilen.");

			// determine the column separator
			var sep = LoadProfiles.getSeparator(rows);
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
			var hourlyTemperature = new double[Stats.HOURS];
			for (int row = 1; row < rows.size(); row++) {
				var line = rows.get(row);
				if (Strings.nullOrEmpty(line))
					continue;

				// split the row and check the format
				var parts = line.split(sep);
				if (parts.length < 2)
					return Result.error("Ungültiges Dateiformat: Die Zeile "
							+ (row + 1) + " hat weniger als 2 Spalten");

				// parse the numbers
				try {
					int idx = Integer.parseInt(parts[0], 10) - 1;
					double temperature = Double.parseDouble(parts[1].replace(',', '.'));

					if (idx < 0 || idx >= Stats.HOURS)
						return Result.error("Ungültige Stunde in Zeile "
								+ (row + 1) + ": " + (idx + 1));
					hourlyTemperature[idx] = temperature;
				} catch (Exception e) {
					return Result.error("Die Zahlen in Zeile "
							+ (row + 1) + " konnten nicht gelesen werden.");
				}
			} 

			return warning != null
					? Result.warning(hourlyTemperature, warning)
					: Result.ok(hourlyTemperature);

		} catch (Exception e) {
			return Result.error(
					"Die Datei konnte nicht gelesen werden: " + e.getMessage());
		}
	}
}
