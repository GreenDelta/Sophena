package sophena.db.daos;

import sophena.db.Database;
import sophena.model.Fuel;

/// @deprecated hollow class, database methods can be used instead
@Deprecated
public class FuelDao extends RootEntityDao<Fuel> {

	public FuelDao(Database db) {
		super(Fuel.class, db);
	}

}
