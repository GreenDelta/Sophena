package sophena.db.daos;

import java.util.ArrayList;
import java.util.List;

import sophena.db.Database;
import sophena.db.NativeSql;
import sophena.model.Descriptor;
import sophena.model.ModelType;
import sophena.model.RootEntity;

/**
 * Dao for root entities that live in a project.
 */
class ProjectEntityDao<T extends RootEntity> extends RootEntityDao<T> {

	public ProjectEntityDao(Class<T> type, Database db) {
		super(type, db);
	}

	public List<Descriptor> getProjectContent(String projectId) {
		String sql = "SELECT id, name, description FROM " + getTable()
				+ " WHERE f_project = '" + projectId + "'";
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
