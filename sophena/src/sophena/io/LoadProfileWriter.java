package sophena.io;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.model.LoadProfile;
import sophena.model.Stats;
import sophena.utils.Num;

public class LoadProfileWriter {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final char decimalSeparator;

	public LoadProfileWriter() {
		decimalSeparator = Num.getFormat()
				.getDecimalFormatSymbols()
				.getDecimalSeparator();
	}

	public void write(LoadProfile profile, File file) {
		if (profile == null || file == null) {
			log.error("file ({}) or profile ({}) is null", file, profile);
			return;
		}
		try {
			var rows = makeRows(profile);
			Files.write(file.toPath(), rows);
		} catch (Exception e) {
			log.error("failed to write profile to file " + file, e);
		}
	}

	private List<String> makeRows(LoadProfile p) {
		var rows = new ArrayList<String>();
		rows.add("Stunde;Heizwaerme;Warmwasser;Gesamt");
		StringBuilder row = new StringBuilder();
		double[] totals = p.calculateTotal();
		for (int i = 0; i < Stats.HOURS; i++) {
			row.append(i + 1).append(';');
			row.append(str(p.dynamicData, i)).append(';');
			row.append(str(p.staticData, i)).append(';');
			row.append(str(totals, i));
			rows.add(row.toString());
			row = new StringBuilder();
		}
		return rows;
	}

	private String str(double[] array, int i) {
		double val = Stats.get(array, i);
		String s = String.format("%.2f", val);
		if (decimalSeparator != '.')
			s = s.replace('.', decimalSeparator);
		return s;
	}
}
