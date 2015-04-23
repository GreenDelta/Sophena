package sophena.db.daos;

import java.util.ArrayList;
import java.util.List;

import sophena.db.Database;
import sophena.db.NativeSql;
import sophena.model.Descriptor;
import sophena.model.ModelType;
import sophena.model.RootEntity;

public class RootEntityDao<T extends RootEntity> extends Dao<T> {

	public RootEntityDao(Class<T> type, Database db) {
		super(type, db);
	}

	public List<Descriptor> getDescriptors() {
		String sql = "SELECT id, name, description FROM " + getTable();
		List<Descriptor> list = new ArrayList<>();
		ModelType type = ModelType.forModelClass(getType());
		try {
			NativeSql.on(db).query(sql, (r) -> {
				Descriptor d = new Descriptor();
				d.setId(r.getString(1));
				d.setName(r.getString(2));
				d.setDescription(r.getString(3));
				d.setType(type);
				list.add(d);
				return true;
			});
		} catch (Exception e) {
			log.error("failed to get descriptors for " + getType(), e);
		}
		return list;
	}
}