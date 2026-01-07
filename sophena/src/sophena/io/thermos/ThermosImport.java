package sophena.io.thermos;

import java.util.Objects;

import sophena.db.Database;
import sophena.model.Project;

public class ThermosImport implements Runnable {

	private final Database db;
	private final ThermosImportConfig config;
	private final ThermosFile file;
	private final Project project;
	private String error;

	public ThermosImport(
		Database db, ThermosImportConfig config, ThermosFile file) {
		this.db = Objects.requireNonNull(db);
		this.config = Objects.requireNonNull(config);
		this.file = Objects.requireNonNull(file);
		this.project = Objects.requireNonNull(config.project());
	}

	public boolean hasError() {
		return error != null;
	}

	public String error() {
		return error;
	}

	@Override
	public void run() {
		try {
			if (config.isWithConsumers()) {
				syncConsumers();
			}
		} catch (Exception e) {
			error = "Unerwarteter Fehler im Import: " + e.getMessage();
		}
	}

	private void syncConsumers() {

	}
}
