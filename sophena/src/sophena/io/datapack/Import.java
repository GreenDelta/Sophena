package sophena.io.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sophena.db.Database;
import sophena.db.daos.Dao;
import sophena.model.Fuel;
import sophena.model.ModelType;

public class Import implements Runnable {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Database db;
	private DataPack pack;

	public Import(DataPack pack, Database db) {
		this.pack = pack;
		this.db = db;
	}

	@Override
	public void run() {
		try {
			importFuels();
		} catch (Exception e) {
			log.error("failed to import data pack " + pack, e);
		}
	}

	private void importFuels() {
		Gson gson = new Gson();
		Dao<Fuel> dao = new Dao<>(Fuel.class, db);
		for(String fuelId : pack.getIds(ModelType.FUEL))
			importFuel(fuelId, gson, dao);
	}

	private void importFuel(String fuelId, Gson gson, Dao<Fuel> dao) {
		try {
			if(dao.contains(fuelId)) {
				log.trace("Fuel {} already exists: not imported");
				return;
			}
			JsonObject obj = pack.get(ModelType.FUEL, fuelId);
			Fuel fuel = gson.fromJson(obj, Fuel.class);
			dao.insert(fuel);
		} catch (Exception e) {
			log.error("failed to import fuel " + fuelId, e);
		}
	}
}
