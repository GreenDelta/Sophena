package sophena.model;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.eclipse.persistence.annotations.Convert;
import org.eclipse.persistence.annotations.Converter;

/**
 * A producer profile provides the minimum and maximum power of a producer for
 * each hour in a year.
 */
@Entity
@Table(name = "tbl_producer_profiles")
@Converter(name = "DoubleArrayConverter", converterClass = DoubleArrayConverter.class)
public class ProducerProfile extends AbstractEntity {

	@Column(name = "min_power")
	@Convert("DoubleArrayConverter")
	public double[] minPower;

	@Column(name = "max_power")
	@Convert("DoubleArrayConverter")
	public double[] maxPower;

	@Override
	public ProducerProfile clone() {
		ProducerProfile clone = new ProducerProfile();
		clone.id = UUID.randomUUID().toString();
		clone.minPower = Stats.copy(minPower);
		clone.maxPower = Stats.copy(maxPower);
		return clone;
	}

	public static ProducerProfile read(File file) {
		try {
			ProducerProfile profile = new ProducerProfile();
			profile.id = UUID.randomUUID().toString();
			profile.maxPower = new double[Stats.HOURS];
			profile.minPower = new double[Stats.HOURS];
			List<String> lines = Files.readAllLines(file.toPath());
			for (int i = 0; i < lines.size(); i++) {
				if (i == 0) // skip header
					continue;
				String line = lines.get(i);
				String[] parts = null;
				if (line.contains(";")) {
					parts = line.split(";");
				} else {
					parts = line.split(",");
				}
				if (parts.length < 3)
					continue;
				int hour = ((int) num(parts[0])) - 1;
				if (hour < 0 || hour >= Stats.HOURS)
					continue;
				profile.maxPower[hour] = num(parts[1]);
				profile.minPower[hour] = num(parts[2]);
			}
			return profile;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static double num(String s) {
		if (s == null)
			return 0;
		return Double.parseDouble(s.replace(",", "."));
	}
}
