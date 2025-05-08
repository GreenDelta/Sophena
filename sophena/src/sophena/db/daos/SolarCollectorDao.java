package sophena.db.daos;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import sophena.db.Database;
import sophena.model.SolarCollector;
import sophena.model.ProductType;

public class SolarCollectorDao extends RootEntityDao<SolarCollector> {

	public SolarCollectorDao(Database db) {
		super(SolarCollector.class, db);
	}

	public List<SolarCollector> getAll(ProductType type) {
		if (type == null)
			return Collections.emptyList();
		return getAll().stream()
				.filter(s -> s.type == type)
				.collect(Collectors.toList());
	}

}
