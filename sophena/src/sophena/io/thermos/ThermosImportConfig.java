package sophena.io.thermos;

import sophena.model.Manufacturer;
import sophena.model.Project;

public class ThermosImportConfig {

	private final Project project;
	private ThermosFile thermosFile;
	private boolean withConsumers = true;
	private boolean withStations = true;
	private boolean withPipes = true;
	private boolean updateExisting = true;

	private sophena.model.Manufacturer stationManufacturer;
	private String stationProductLine;
	private sophena.model.Manufacturer pipeManufacturer;
	private String pipeProductLine;

	public ThermosImportConfig(Project project) {
		this.project = project;
	}

	public Project project() {
		return project;
	}

	public ThermosFile thermosFile() {
		return thermosFile;
	}

	public void withThermosFile(ThermosFile thermosFile) {
		this.thermosFile = thermosFile;
	}

	public boolean isWithConsumers() {
		return withConsumers;
	}

	public void withConsumers(boolean withConsumers) {
		this.withConsumers = withConsumers;
	}

	public boolean isWithStations() {
		return withStations;
	}

	public void withStations(boolean withStations) {
		this.withStations = withStations;
	}

	public boolean isWithPipes() {
		return withPipes;
	}

	public void withPipes(boolean withPipes) {
		this.withPipes = withPipes;
	}

	public boolean isUpdateExisting() {
		return updateExisting;
	}

	public void updateExisting(boolean updateExisting) {
		this.updateExisting = updateExisting;
	}

	public Manufacturer stationManufacturer() {
		return stationManufacturer;
	}

	public void stationManufacturer(Manufacturer manufacturer) {
		this.stationManufacturer = manufacturer;
	}

	public String stationProductLine() {
		return stationProductLine;
	}

	public void stationProductLine(String productLine) {
		this.stationProductLine = productLine;
	}

	public Manufacturer pipeManufacturer() {
		return pipeManufacturer;
	}

	public void pipeManufacturer(Manufacturer manufacturer) {
		this.pipeManufacturer = manufacturer;
	}

	public String pipeProductLine() {
		return pipeProductLine;
	}

	public void pipeProductLine(String productLine) {
		this.pipeProductLine = productLine;
	}

	public boolean canRunImport() {
		if (thermosFile == null || thermosFile().isEmpty()) return false;
		if (withStations && stationProductLine == null) return false;
		if (withPipes && pipeProductLine == null) return false;
		return withConsumers || withStations || withPipes;
	}
}
