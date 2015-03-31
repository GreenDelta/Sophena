package sophena.io;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.model.Stats;
import sophena.rcp.Numbers;
import sophena.rcp.utils.Strings;

/**
 * Reads data from an hours profile. A hours profile file should have the
 * following format:
 * <p>
 * <ul>
 * <li>one row with column headers + 8760 data rows</li>
 * <li>at least two columns: the first column contains the values 1..8760 which
 * may are not sorted. The second column contains the values. Other columns are
 * ignored.</li>
 * </ul>
 * <p>
 * We try to identify the column separator in the reader.
 */
public final class HoursProfile {

	public static double[] read(File file) {
		return new Reader().read(file);
	}

	public static void write(double[] profile, File file) {
		new Writer().write(profile, file);
	}

	private static class Writer {

		private Logger log = LoggerFactory.getLogger(getClass());

		public void write(double[] profile, File file) {
			log.info("write profile to file {}", file);
			try {
				List<String> rows = new ArrayList<>();
				writeRows(profile, rows);
				Files.write(file.toPath(), rows);
			} catch (Exception e) {
				log.error("failed to write profile to file " + file, e);
			}
		}

		private void writeRows(double[] profile, List<String> rows) {
			rows.add("index;value");
			char s = Numbers.getFormat().getDecimalFormatSymbols()
					.getDecimalSeparator();
			StringBuilder row = new StringBuilder();
			for (int i = 0; i < profile.length; i++) {
				row.append(i + 1).append(';');
				String val = Double.toString(profile[i]);
				if (s != '.')
					val = val.replace('.', s);
				row.append(val);
				rows.add(row.toString());
				row = new StringBuilder();
			}
		}
	}

	private static class Reader {

		private Logger log = LoggerFactory.getLogger(getClass());

		public double[] read(File file) {
			log.info("read profile from file {}", file);
			double[] profile = new double[Stats.HOURS];
			try {
				List<String> rows = Files.readAllLines(file.toPath());
				if (rows.size() != (Stats.HOURS + 1)) {
					log.warn("file contains {} rows but should contain {}",
							rows.size(), Stats.HOURS);
				}
				readRows(rows, profile);
			} catch (Exception e) {
				log.error("failed to read hours profile from " + file, e);
			}
			return profile;
		}

		private void readRows(List<String> rows, double[] profile) {
			for (int row = 1; row < rows.size(); row++) {
				String line = rows.get(row);
				Entry e = Entry.read(rows.get(row));
				if (e == null || !e.isValid()) {
					log.warn("invalid entry {} at row {}", line, row);
					continue;
				}
				profile[e.index] = e.value;
			}
		}

		private static class Entry {
			int index;
			double value;

			Entry(int index, double value) {
				this.index = index;
				this.value = value;
			}

			boolean isValid() {
				return 0 <= index && index < Stats.HOURS;
			}

			static Entry read(String row) {
				if (Strings.nullOrEmpty(row))
					return new Entry(-1, 0);
				StringBuilder i = new StringBuilder();
				StringBuilder v = new StringBuilder();
				boolean index = true;
				char sep = ';';
				for (char c : row.trim().toCharArray()) {
					if (index) {
						if (Character.isDigit(c))
							i.append(c);
						else {
							index = false;
							sep = c;
						}
					} else {
						if (c == sep)
							break;
						else
							v.append(c);
					}
				}
				return makeEntry(i.toString(), v.toString());
			}

			private static Entry makeEntry(String i, String v) {
				try {
					int index = Integer.parseInt(i, 10);
					double val = Double.parseDouble(v.replace(',', '.'));
					return new Entry(index - 1, val);
				} catch (Exception e) {
					return new Entry(-1, 0);
				}
			}
		}
	}
}
