package sophena.db.daos;

import sophena.db.Database;
import sophena.model.Boiler;

public class BoilerDao extends RootEntityDao<Boiler> {

	public BoilerDao(Database db) {
		super(Boiler.class, db);
	}

}
