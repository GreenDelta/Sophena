package sophena.db.daos;

import sophena.db.Database;
import sophena.model.Producer;

public class ProducerDao extends ProjectEntityDao<Producer> {

	public ProducerDao(Database db) {
		super(Producer.class, db);
	}

}
