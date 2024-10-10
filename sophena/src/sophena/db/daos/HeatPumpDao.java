package sophena.db.daos;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import sophena.db.Database;
import sophena.model.HeatPump;
import sophena.model.ProductType;

public class HeatPumpDao extends RootEntityDao<HeatPump> {

	public HeatPumpDao(Database db) {
		super(HeatPump.class, db);
	}

	public List<HeatPump> getAll(ProductType type) {
		if (type == null)
			return Collections.emptyList();
		return getAll().stream()
				.filter(s -> s.type == type)
				.collect(Collectors.toList());
	}

}
