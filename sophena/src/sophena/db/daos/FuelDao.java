package sophena.db.daos;

import sophena.db.Database;
import sophena.model.Fuel;

public class FuelDao extends RootEntityDao<Fuel> {

	public FuelDao(Database db) {
		super(Fuel.class, db);
	}

}
