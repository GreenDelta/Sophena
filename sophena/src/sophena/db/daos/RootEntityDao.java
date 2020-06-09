package sophena.db.daos;

import sophena.db.Database;
import sophena.model.RootEntity;

public class RootEntityDao<T extends RootEntity> extends Dao<T> {

	public RootEntityDao(Class<T> type, Database db) {
		super(type, db);
	}

}
