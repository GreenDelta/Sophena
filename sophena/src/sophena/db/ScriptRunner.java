package sophena.db;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * A class for running a SQL scripts with insert, update, delete, and DDL
 * statements on a database connection.
 */
class ScriptRunner implements ScriptHandler {

	private final int MAX_BATCH_SIZE = 1000;
	private Database database;
	private List<String> statements = new ArrayList<>();

	public ScriptRunner(Database database) {
		this.database = database;
	}

	public void run(InputStream scriptStream, String encoding) throws Exception {
		try {
			InputStreamReader reader = new InputStreamReader(scriptStream,
					encoding);
			ScriptParser parser = new ScriptParser(this);
			parser.parse(reader);
			execBatch();
		} catch (Exception e) {
			throw new Exception("Cannot execute script: " + e.getMessage(), e);
		}
	}

	@Override
	public void statement(String query) throws Exception {
		statements.add(query);
		if (statements.size() >= MAX_BATCH_SIZE)
			execBatch();
	}

	private void execBatch() throws Exception {
		if (statements.isEmpty())
			return;
		NativeSql.on(database).batchUpdate(statements);
		statements.clear();
	}
}
