package sophena.db.daos;

import sophena.db.Database;
import sophena.model.Consumer;

public class ConsumerDao extends ProjectEntityDao<Consumer> {

	public ConsumerDao(Database db) {
		super(Consumer.class, db);
	}

}