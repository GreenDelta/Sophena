package sophena.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.model.Stats;

public class ClimateFileReader implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private File file;
	private ClimateFileSettings settings;

	private ClimateFileResult result;
	private Map<String, Integer> index;
	private double[] data;
	private double[] directRadiation;
	private double[] diffuseRadiation;
	private int[] items;

	public ClimateFileReader(File file) {
		this(file, null);
	}

	public ClimateFileReader(File file, ClimateFileSettings settings) {
		this.file = file;
		this.settings = settings;
		if (settings == null)
			this.settings = ClimateFileSettings.getDefault();
		if (this.settings.getSeparator() == null)
			this.settings.setSeparator(";");
	}

	public ClimateFileResult getResult() {
		return result;
	}

	@Override
	public void run() {
		setUp();
		try (Reader reader = new FileReader(file);
				BufferedReader buffer = new BufferedReader(reader)) {
			readLines(buffer);
		} catch (Exception e) {
			log.error("failed to reade climate file", e);
			result.setWithoutError(false);
		}
		finish();
	}

	private void finish() {
		for (int i = 0; i < data.length; i++) {
			int n = items[i];
			if (n == 0)
				continue;
			data[i] = data[i] / ((double) n);
			directRadiation[i] = directRadiation[i] / (double) n;
			diffuseRadiation[i] = diffuseRadiation[i] / (double) n;
		}
		int expected = settings.getEndYear() - settings.getStartYear() + 1;
		int min = Stats.min(items);
		if (min < expected)
			result.setComplete(false);
	}

	private void readLines(BufferedReader buffer) throws Exception {
		buffer.readLine(); // header
		String line;
		int rowNum = 1;
		while ((line = buffer.readLine()) != null) {
			rowNum++;
			if (!line.contains(settings.getSeparator()))
				continue;
			String[] row = line.split(settings.getSeparator());
			String date = getDate(row);
			if (date == null) {
				log.warn("invalid date format at row {}", rowNum);
				result.setWithoutError(false);
				continue;
			}
			int year = getYear(date);
			if (year < settings.getStartYear() || year > settings.getEndYear())
				continue;
			int idx = getIndex(date);
			if (idx < 0)
				continue;
			
			Double val = getValue(row);
			if (val == null || val < -100 || val > 100) {
				log.warn("invalid temperature {} at row {}", val, rowNum);
				result.setWithoutError(false);
				continue;
			}

			Double directRad = getValue(row);
			Double diffuseRad = getValue(row);

			data[idx] = val;
			directRadiation[idx] = directRad;
			diffuseRadiation[idx] = diffuseRad;
			items[idx]++;
		}
	}

	private Double getValue(String[] row) {
		if (row == null)
			return null;
		if (settings.getTemperatureColumn() >= row.length)
			return null;
		String val = row[settings.getTemperatureColumn()].trim();
		try {
			return Double.parseDouble(val);
		} catch (Exception e) {
			log.warn("invalid temperature format " + val, e);
			return null;
		}
	}

	private int getIndex(String date) {
		String key = date.substring(4);
		Integer idx = index.get(key);
		if (idx != null)
			return idx;
		log.warn("invalid date format {}", date);
		return -1;
	}

	private int getYear(String date) {
		try {
			String part = date.substring(0, 4);
			return Integer.parseInt(part, 10);
		} catch (Exception e) {
			log.warn("failed to get year from date: " + date, e);
			result.setWithoutError(false);
			return -1;
		}
	}

	private String getDate(String[] row) {
		if (row == null)
			return null;
		if (settings.getDateColumn() >= row.length)
			return null;
		String date = row[settings.getDateColumn()].trim();
		if (date.length() != 10)
			return null;
		for (int i = 0; i < date.length(); i++) {
			if (!Character.isDigit(date.charAt(i)))
				return null;
		}
		return date;
	}

	private void setUp() {
		result = new ClimateFileResult();
		data = new double[Stats.HOURS];
		directRadiation = new double[Stats.HOURS];
		diffuseRadiation = new double[Stats.HOURS];
		items = new int[Stats.HOURS];
		result.setData(data);
		result.setDirectRadiation(directRadiation);
		result.setDiffuseRadiation(diffuseRadiation);
		result.setItemsPerDatum(items);
		result.setWithoutError(true);
		index = createIndex();
	}

	private Map<String, Integer> createIndex() {
		String[] months = { "01", "02", "03", "04", "05", "06",
				"07", "08", "09", "10", "11", "12" };
		int[] days = { 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
		Map<String, Integer> index = new HashMap<>(Stats.HOURS);
		int idx = 0;
		for (int month = 0; month < months.length; month++) {
			for (int day = 1; day <= days[month]; day++) {
				for (int hour = 0; hour < 24; hour++) {
					String key = months[month] + format(day) + format(hour);
					index.put(key, idx);
					idx++;
				}
			}
		}
		return index;
	}

	private String format(int val) {
		if (val < 10)
			return "0" + val;
		else
			return Integer.toString(val);
	}
}
