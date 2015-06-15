package sophena.db.daos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sophena.db.Database;
import sophena.db.NativeSql;
import sophena.model.Consumer;
import sophena.model.descriptors.ConsumerDescriptor;
import sophena.model.descriptors.ProjectDescriptor;

public class ConsumerDao extends RootEntityDao<Consumer> {

	public ConsumerDao(Database db) {
		super(Consumer.class, db);
	}

	public List<ConsumerDescriptor> getDescriptors(ProjectDescriptor pd) {
		if (pd == null)
			return Collections.emptyList();
		String sql = "SELECT id, name, description, is_disabled FROM tbl_consumers "
				+ " WHERE f_project = '" + pd.getId() + "'";
		List<ConsumerDescriptor> list = new ArrayList<>();
		try {
			NativeSql.on(db).query(sql, (r) -> {
				ConsumerDescriptor d = new ConsumerDescriptor();
				d.setId(r.getString(1));
				d.setName(r.getString(2));
				d.setDescription(r.getString(3));
				d.setDisabled(r.getBoolean(4));
				list.add(d);
				return true;
			});
		} catch (Exception e) {
			log.error("failed to get descriptors for " + getType(), e);
		}
		return list;
	}

}