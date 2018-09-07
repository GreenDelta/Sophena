package sophena.io;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.model.LoadProfile;
import sophena.model.Stats;
import sophena.utils.Strings;

public class LoadProfileReader {

	private Logger log = LoggerFactory.getLogger(getClass());

	public LoadProfile read(File file) {
		if (file == null) {
			log.error("File is null");
			return null;
		}
		LoadProfile profile = LoadProfile.initEmpty();
		try {
			List<String> rows = Files.readAllLines(file.toPath());
			if (rows.size() != (Stats.HOURS + 1)) {
				log.warn("file contains {} rows but should contain {}",
						rows.size(), Stats.HOURS);
			}
			readRows(rows, profile);
		} catch (Exception e) {
			log.error("failed to read load profile" + file, e);
		}
		return profile;
	}

	private void readRows(List<String> rows, LoadProfile profile) {
		if (rows.size() < 2)
			return;
		String sep = getSeparator(rows.get(1));
		for (int row = 1; row < rows.size(); row++) {
			String line = rows.get(row);
			Entry e = Entry.read(rows.get(row), sep);
			if (e == null || !e.isValid()) {
				log.warn("invalid entry {} at row {}", line, row);
				continue;
			}
			profile.dynamicData[e.index] = e.dynamicValue;
			profile.staticData[e.index] = e.staticValue;
		}
	}

	private String getSeparator(String row) {
		if (row == null)
			return ";";
		for (char c : row.trim().toCharArray()) {
			if (Character.isDigit(c) || ' ' == c)
				continue;
			return Character.toString(c);
		}
		return ";";
	}

	private static class Entry {

		int index;
		double dynamicValue;
		double staticValue;

		Entry(int index, double dynamicValue, double staticValue) {
			this.index = index;
			this.dynamicValue = dynamicValue;
			this.staticValue = staticValue;
		}

		boolean isValid() {
			return 0 <= index && index < Stats.HOURS;
		}

		static Entry read(String row, String separator) {
			if (Strings.nullOrEmpty(row))
				return new Entry(-1, 0, 0);
			String[] parts = row.split(separator);
			if (parts.length < 3)
				return new Entry(-1, 0, 0);
			return make(parts[0], parts[1], parts[2]);
		}

		static Entry make(String i, String dynVal, String statVal) {
			try {
				int index = Integer.parseInt(i, 10);
				double dyn = Double.parseDouble(dynVal.replace(',', '.'));
				double stat = Double.parseDouble(statVal.replace(',', '.'));
				return new Entry(index - 1, dyn, stat);
			} catch (Exception e) {
				return new Entry(-1, 0, 0);
			}
		}
	}
}
