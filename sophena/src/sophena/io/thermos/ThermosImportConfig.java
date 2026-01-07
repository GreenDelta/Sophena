package sophena.io.thermos;

public class ThermosImportConfig {

	private ThermosFile thermosFile;
	private boolean withConsumers = true;
	private boolean withStations = true;
	private boolean withPipes = true;
	private boolean updateExisting = true;

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

	public void setUpdateExisting(boolean updateExisting) {
		this.updateExisting = updateExisting;
	}

	public boolean canRunImport() {
		if (thermosFile == null || thermosFile().isEmpty())
			return false;
		return withConsumers || withStations || withPipes;
	}
}
