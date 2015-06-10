package sophena.db.daos;

import sophena.db.Database;
import sophena.db.NativeSql;
import sophena.model.CostSettings;

public class CostSettingsDao extends Dao<CostSettings> {

	public CostSettingsDao(Database db) {
		super(CostSettings.class, db);
	}

	public CostSettings getGlobal() {
		String sql = "SELECT id FROM tbl_cost_settings WHERE is_global = true";
		try {
			String[] id = new String[1];
			NativeSql.on(db).query(sql, (r) -> {
				id[0] = r.getString(1);
				return false;
			});
			return id[0] == null ? null : get(id[0]);
		} catch (Exception e) {
			log.error("failed to load global cost settings", e);
			return null;
		}
	}
}
