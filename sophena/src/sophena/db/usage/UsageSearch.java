package sophena.db.usage;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.Database;
import sophena.db.NativeSql;
import sophena.model.Boiler;
import sophena.model.ModelType;

/**
 * Searches for the usage of entities in other entities.
 */
public class UsageSearch {

	private Database database;

	public UsageSearch(Database database) {
		this.database = database;
	}

	public List<SearchResult> of(Boiler boiler) {
		if (boiler == null || boiler.id == null)
			return Collections.emptyList();
		String sql = "select p.id, p.name from tbl_producers p "
				+ "where f_boiler = '" + boiler.id + "'";
		List<SearchResult> list = new ArrayList<>();
		query(sql, r -> list.add(
				new SearchResult(str(r, 1), str(r, 2), ModelType.PRODUCER)));
		return list;
	}

	private String str(ResultSet r, int i) {
		try {
			return r.getString(i);
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to result set field from " + r, e);
			return "";
		}
	}

	private void query(String sql, Consumer<ResultSet> fn) {
		try {
			NativeSql.on(database).query(sql, result -> {
				fn.accept(result);
				return true;
			});
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to execute query: " + sql, e);
		}
	}
}
