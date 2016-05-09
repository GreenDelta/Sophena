package sophena.db.daos;

import java.util.ArrayList;
import java.util.List;

import sophena.db.Database;
import sophena.db.NativeSql;
import sophena.model.Project;
import sophena.model.descriptors.CleaningDescriptor;
import sophena.model.descriptors.ProjectDescriptor;

public class ProjectDao extends RootEntityDao<Project> {

	public ProjectDao(Database db) {
		super(Project.class, db);
	}

	public List<ProjectDescriptor> getDescriptors() {
		String sql = "SELECT id, name, description FROM tbl_projects";
		List<ProjectDescriptor> list = new ArrayList<>();
		try {
			NativeSql.on(db).query(sql, r -> {
				ProjectDescriptor d = new ProjectDescriptor();
				d.id = r.getString(1);
				d.name = r.getString(2);
				d.description = r.getString(3);
				list.add(d);
				return true;
			});
		} catch (Exception e) {
			log.error("failed to get descriptors for " + getType(), e);
		}
		return list;
	}

	/**
	 * Returns the descriptors of the flue gas cleaning facilities of a project.
	 */
	public List<CleaningDescriptor> getCleaningDescriptors(String projectId) {
		String sql = "SELECT e.id, c.name, c.description FROM "
				+ "tbl_flue_gas_cleaning_entries e INNER JOIN "
				+ "tbl_flue_gas_cleaning c ON e.f_flue_gas_cleaning = c.id "
				+ "WHERE e.f_project = '" + projectId + "'";
		List<CleaningDescriptor> list = new ArrayList<>();
		try {
			NativeSql.on(db).query(sql, r -> {
				CleaningDescriptor d = new CleaningDescriptor();
				d.id = r.getString(1);
				d.name = r.getString(2);
				d.description = r.getString(3);
				list.add(d);
				return true;
			});
		} catch (Exception e) {
			log.error("failed to get flue gas cleaning descriptors", e);
		}
		return list;
	}

}
