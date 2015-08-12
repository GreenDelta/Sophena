package sophena.db.daos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sophena.db.Database;
import sophena.db.NativeSql;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;

public class ProjectDao extends RootEntityDao<Project> {

	public ProjectDao(Database db) {
		super(Project.class, db);
	}

	public List<ProjectDescriptor> getDescriptors() {
		String sql = "SELECT id, name, description, is_variant "
				+ "FROM tbl_projects";
		List<ProjectDescriptor> list = new ArrayList<>();
		try {
			NativeSql.on(db).query(sql, (r) -> {
				ProjectDescriptor d = new ProjectDescriptor();
				d.id = r.getString(1);
				d.setName(r.getString(2));
				d.setDescription(r.getString(3));
				d.setVariant(r.getBoolean(4));
				list.add(d);
				return true;
			});
		} catch (Exception e) {
			log.error("failed to get descriptors for " + getType(), e);
		}
		return list;
	}

	public List<ProjectDescriptor> getVariantDescriptors(ProjectDescriptor pd) {
		if (pd == null)
			return Collections.emptyList();
		String sql = "SELECT id, name, description FROM tbl_projects WHERE "
				+ " f_project = '" + pd.id + "'";
		List<ProjectDescriptor> list = new ArrayList<>();
		try {
			NativeSql.on(db).query(sql, (r) -> {
				ProjectDescriptor d = new ProjectDescriptor();
				d.id = r.getString(1);
				d.setName(r.getString(2));
				d.setDescription(r.getString(3));
				d.setVariant(true);
				list.add(d);
				return true;
			});
		} catch (Exception e) {
			log.error("failed to get descriptors for " + getType(), e);
		}
		return list;
	}

}
