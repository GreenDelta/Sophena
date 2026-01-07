package sophena.io.thermos.wizard;

import java.io.File;

public class ImportConfig {

	private File file;
	private boolean importConsumers;
	private boolean importTransferStations;
	private boolean importPipes;
	private boolean updateExisting = true;

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public boolean isImportConsumers() {
		return importConsumers;
	}

	public void setImportConsumers(boolean importConsumers) {
		this.importConsumers = importConsumers;
	}

	public boolean isImportTransferStations() {
		return importTransferStations;
	}

	public void setImportTransferStations(boolean importTransferStations) {
		this.importTransferStations = importTransferStations;
	}

	public boolean isImportPipes() {
		return importPipes;
	}

	public void setImportPipes(boolean importPipes) {
		this.importPipes = importPipes;
	}

	public boolean isUpdateExisting() {
		return updateExisting;
	}

	public void setUpdateExisting(boolean updateExisting) {
		this.updateExisting = updateExisting;
	}
}
