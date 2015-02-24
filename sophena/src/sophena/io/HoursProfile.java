package sophena.io;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HoursProfile {

	private static final int ITEMS = 8760;

	public static double[] read(File file) {
		return new Reader().read(file);
	}

	private static class Reader {

		private Logger log = LoggerFactory.getLogger(getClass());

		public double[] read(File file) {
			double[] profile = new double[ITEMS];
			try {
				List<String> rows = Files.readAllLines(file.toPath());
				if (rows.size() != ITEMS) {
					log.warn("file contains {} rows but should contain {}",
							rows.size(), ITEMS);
				}
				for (int row = 0; row < rows.size(); row++) {
					if (row >= ITEMS)
						break;
					profile[row] = readNumber(rows.get(row));
				}
			} catch (Exception e) {
				log.error("failed to read hours profile from " + file, e);
			}
			return profile;
		}

		private double readNumber(String s) {
			if (s == null)
				return 0;
			try {
				String n = s.trim().replace(',', '.');
				return Double.parseDouble(n);
			} catch (Exception e) {
				log.error(s + " is not a valid entry", e);
				return 0;
			}
		}
	}
}
