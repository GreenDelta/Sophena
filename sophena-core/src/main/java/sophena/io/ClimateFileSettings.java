package sophena.io;

public class ClimateFileSettings {

	private String separator = ";";
	private int dateColumn = 1;
	private int temperatureColumn = 5;
	private int startYear = 1994;
	private int endYear = 2013;

	public static ClimateFileSettings getDefault() {
		return new ClimateFileSettings();
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public int getDateColumn() {
		return dateColumn;
	}

	public void setDateColumn(int dateColumn) {
		this.dateColumn = dateColumn;
	}

	public int getTemperatureColumn() {
		return temperatureColumn;
	}

	public void setTemperatureColumn(int temperatureColumn) {
		this.temperatureColumn = temperatureColumn;
	}

	public int getStartYear() {
		return startYear;
	}

	public void setStartYear(int startYear) {
		this.startYear = startYear;
	}

	public int getEndYear() {
		return endYear;
	}

	public void setEndYear(int endYear) {
		this.endYear = endYear;
	}
}
