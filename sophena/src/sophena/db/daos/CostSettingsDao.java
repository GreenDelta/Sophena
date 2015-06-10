package sophena.db.daos;

import sophena.db.Database;
import sophena.model.CostSettings;

public class CostSettingsDao extends Dao<CostSettings> {

	public CostSettingsDao(Database db) {
		super(CostSettings.class, db);
	}

	public CostSettings getGlobal() {
		return get(CostSettings.GLOBAL_ID);
	}
}
