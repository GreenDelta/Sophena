package sophena.io.thermos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import sophena.model.Consumer;
import sophena.model.Manufacturer;
import sophena.model.Pipe;
import sophena.model.Project;

public class ThermosImportConfig {

	private final Project project;
	private ThermosFile thermosFile;
	private boolean withConsumers = true;
	private boolean withStations = true;
	private boolean withPipes = true;
	private boolean skipPipes;
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

	/// Indicates that the import of the pipes should be skipped. This is set
	/// when the user decides to continue an import although the selected
	/// product line cannot dimension the network.
	public boolean isSkipPipes() {
		return skipPipes;
	}

	public void skipPipes(boolean skipPipes) {
		this.skipPipes = skipPipes;
	}

	/// Returns the pipes of the given list that belong to the pipe product line
	/// that is selected for this import. Returns an empty list when no
	/// manufacturer is selected.
	public List<Pipe> pipesForProductLine(List<Pipe> pipes) {
		var result = new ArrayList<Pipe>();
		if (pipes == null || pipeManufacturer == null) return result;
		for (var p : pipes) {
			if (p == null) continue;
			if (!Objects.equals(p.manufacturer, pipeManufacturer)) continue;
			if (!Objects.equals(p.productLine, pipeProductLine)) continue;
			result.add(p);
		}
		return result;
	}

	public boolean canRunImport() {
		if (thermosFile == null || thermosFile().isEmpty()) return false;
		if (withStations && stationProductLine == null) return false;
		if (withPipes && pipeProductLine == null) return false;
		return withConsumers || withStations || withPipes;
	}

	/// Returns the consumers that will receive a transfer station when the import
	/// is executed. In append mode only new consumers are added, while in update
	/// mode existing consumers are updated and get a new station when they do not
	/// have one yet or when their heating load changed.
	public List<Consumer> consumersForStationAssignment() {
		if (thermosFile == null)
			return List.of();
		var result = new ArrayList<Consumer>();

		if (!updateExisting) {
			var existingIds = new HashSet<String>();
			for (var c : project.consumers) {
				existingIds.add(c.id);
			}
			for (var c : thermosFile.consumers()) {
				if (!existingIds.contains(c.id)) {
					result.add(c);
				}
			}
			return result;
		}

		var existing = new HashMap<String, Consumer>();
		for (var c : project.consumers) {
			existing.put(c.id, c);
		}
		for (var c : thermosFile.consumers()) {
			var old = existing.get(c.id);
			if (old == null
				|| old.transferStation == null
				|| old.heatingLoad != c.heatingLoad) {
				result.add(c);
			}
		}
		return result;
	}
}
