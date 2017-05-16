package sophena.db.daos;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import sophena.db.Database;
import sophena.model.Boiler;
import sophena.model.ProductType;

public class BoilerDao extends RootEntityDao<Boiler> {

	public BoilerDao(Database db) {
		super(Boiler.class, db);
	}

	public List<Boiler> getAll(ProductType type) {
		if (type == null)
			return Collections.emptyList();
		return getAll().stream()
				.filter(b -> b.type == type)
				.collect(Collectors.toList());
	}

}
