package sophena.io;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.model.Stats;
import sophena.utils.Strings;
import sophena.utils.Num;

/**
 * Reads or writes data from an hours profile. A hours profile file should have the
 * following format:
 * <p>
 * <ul>
 * <li>one row with column headers + 8760 data rows</li>
 * <li>at least two columns: the first column contains the values 1..8760 which
 * may are not sorted. Subsequent columns contains the values.</li>
 * </ul>
 * <p>
 * We try to identify the column separator in the reader.
 */
public final class HoursProfile {

	@Deprecated
	public static double[] read(File file) {
		return new Reader().read(file);
	}

	public static double[][] read2(File file) {
		return new Reader().read2(file);
	}

	@Deprecated
	public static void write(double[] profile, File file) {
		new Writer().write(profile, file);
	}
	
	public static void write(File file, String[] headers, double[]... profiles) {
		new Writer().write(file, headers, profiles);
	}

	private static class Writer {

		private Logger log = LoggerFactory.getLogger(getClass());

		@Deprecated
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
		
		public void write(File file, String[] headers, double[]... profiles)
		{
			log.info("write profiles to file {}", file);
			try {
				List<String> rows = new ArrayList<>();
				writeRows(rows, headers, profiles);
				Files.write(file.toPath(), rows);
			} catch (Exception e) {
				log.error("failed to write profiles to file " + file, e);
			}
		}

		@Deprecated
		private void writeRows(double[] profile, List<String> rows) {
			rows.add("index;value");
			char s = Num.getFormat().getDecimalFormatSymbols()
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

		private void writeRows(List<String> rows, String[] headers, double[]... profiles) {
			
			if(profiles.length != headers.length - 1)
				throw new IllegalArgumentException("Length of headers array must be one plus length of profiles array");
			
			if(profiles.length == 0)
				throw new IllegalArgumentException("Profiles array needs at least one element");
			
			int profileLength = profiles[0].length;
			for (int i = 1; i < profiles.length; i++)
				if (profiles[i] != null && profiles[i].length != profileLength)
					throw new IllegalArgumentException("Length of all profile arrays must be identical");
			
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < headers.length; i++)
			{
				if(i > 0)
					sb.append(';');
				sb.append(headers[i]);
			}
			rows.add(sb.toString());

			char s = Num.getFormat().getDecimalFormatSymbols()
					.getDecimalSeparator();
			StringBuilder row = new StringBuilder();
			
			for (int i = 0; i < profileLength; i++) {
				row.append(i + 1);
				for(int j = 0; j < profiles.length; j++)
				{
					row.append(';');
					String val = profiles[j] != null ? Double.toString(profiles[j][i]) : "";
					if (s != '.')
						val = val.replace('.', s);
					row.append(val);
				}
				rows.add(row.toString());
				row = new StringBuilder();
			}
		}
	}

	private static class Reader {

		private Logger log = LoggerFactory.getLogger(getClass());

		@Deprecated
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

		public double[][] read2(File file) {
			log.info("read profiles from file {}", file);
			ProfileRow[] profiles = new ProfileRow[Stats.HOURS];
			try {
				List<String> rows = Files.readAllLines(file.toPath());
				if (rows.size() != (Stats.HOURS + 1)) {
					log.warn("file contains {} rows but should contain {}",
							rows.size(), Stats.HOURS);
				}
				readRows2(rows, profiles);
			} catch (Exception e) {
				log.error("failed to read hours profiles from " + file, e);
			}
			
			double[][] profiles2 = new double[profiles[0].cells.length][profiles.length];
			for(int i = 0; i < profiles.length; i++)
				for(int j = 0; j < profiles[i].cells.length; j++)
					profiles2[j][i] = profiles[i].cells[j];
			return profiles2;
		}

		@Deprecated
		private void readRows(List<String> rows, double[] profile) {
			for (int row = 1; row < rows.size(); row++) {
				String line = rows.get(row);
				Entry e = Entry.read(rows.get(row));
				if (e == null || !e.isValid()) {
					log.warn("invalid entry {} at row {}", line, row);
					continue;
				}
				profile[e.index] = e.values[0];
			}
		}

		private void readRows2(List<String> rows, ProfileRow[] profileRows) {
			for (int row = 1; row < rows.size(); row++) {
				String line = rows.get(row);
				Entry e = Entry.read(rows.get(row));
				if (e == null || !e.isValid()) {
					log.warn("invalid entry {} at row {}", line, row);
					continue;
				}
				profileRows[e.index] = new ProfileRow(e.values);
			}
		}
		
		class ProfileRow
		{
			public double[] cells;
			
			public ProfileRow(double[] cells)
			{
				this.cells = cells;
			}
		}

		private static class Entry {
			int index;
			double[] values;

			Entry(int index, double... values) {
				this.index = index;
				this.values = values;
			}

			boolean isValid() {
				return 0 <= index && index < Stats.HOURS;
			}

			static Entry read(String row) {
				if (Strings.nullOrEmpty(row))
					return new Entry(-1, 0);
				StringBuilder i = new StringBuilder();
				StringBuilder v = new StringBuilder();
				ArrayList<String> values = new ArrayList<String>();
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
						{
							values.add(v.toString());
							v = new StringBuilder();
						}
						else
							v.append(c);
					}
				}
				values.add(v.toString());
				return makeEntry(i.toString(), values);
			}

			private static Entry makeEntry(String i, List<String> v) {
				try {
					int index = Integer.parseInt(i, 10);
					double[] values = new double[v.size()];
					for(int j = 0; j < v.size(); j++)
						values[j] = Double.parseDouble(v.get(j).replace(',', '.'));
					if (values.length != 1 && values.length != 3)
						throw new Exception("Invalid value count!");
					return new Entry(index - 1, values);
				} catch (Exception e) {
					return new Entry(-1, 0);
				}
			}
		}
	}
}
