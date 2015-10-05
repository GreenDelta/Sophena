package sophena.db.daos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sophena.db.Database;
import sophena.db.NativeSql;
import sophena.model.Producer;
import sophena.model.descriptors.ProducerDescriptor;
import sophena.model.descriptors.ProjectDescriptor;

public class ProducerDao extends RootEntityDao<Producer> {

	public ProducerDao(Database db) {
		super(Producer.class, db);
	}

	public List<ProducerDescriptor> getDescriptors(ProjectDescriptor pd) {
		if (pd == null)
			return Collections.emptyList();
		String sql = "SELECT id, name, description, is_disabled FROM tbl_producers"
				+ " WHERE f_project = '" + pd.id + "'";
		List<ProducerDescriptor> list = new ArrayList<>();
		try {
			NativeSql.on(db).query(sql, (r) -> {
				ProducerDescriptor d = new ProducerDescriptor();
				d.id = r.getString(1);
				d.name = r.getString(2);
				d.description = r.getString(3);
				d.disabled = r.getBoolean(4);
				list.add(d);
				return true;
			});
		} catch (Exception e) {
			log.error("failed to get descriptors for " + getType(), e);
		}
		return list;
	}

}
