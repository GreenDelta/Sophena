package sophena.db.daos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import sophena.db.Database;
import sophena.db.NativeSql;
import sophena.model.ProjectFolder;
import sophena.model.descriptors.ProjectDescriptor;

public class ProjectFolderDao extends RootEntityDao<ProjectFolder> {

	public ProjectFolderDao(Database db) {
		super(ProjectFolder.class, db);
	}

	/**
	 * Returns the descriptors of all project that have no folder or that have a
	 * folder that does not exist anymore (just to get sure that projects get
	 * never lost).
	 */
	public List<ProjectDescriptor> rootProjects() {
		Set<String> existing = getAll().stream()
				.map(pf -> pf.id)
				.collect(Collectors.toSet());
		String sql = "select id, name, description, "
				+ "f_project_folder from tbl_projects";
		List<ProjectDescriptor> list = new ArrayList<>();
		try {
			NativeSql.on(db).query(sql, r -> {
				String folder = r.getString(4);
				if (folder != null && existing.contains(folder))
					return true;
				ProjectDescriptor d = new ProjectDescriptor();
				d.id = r.getString(1);
				d.name = r.getString(2);
				d.description = r.getString(3);
				list.add(d);
				return true;
			});
		} catch (Exception e) {
			log.error("failed to get rootProjects", e);
		}
		return list;
	}

	/**
	 * Returns the descriptors of all project that are contained in the given
	 * folder
	 */
	public List<ProjectDescriptor> getProjects(ProjectFolder folder) {
		if (folder == null || folder.id == null)
			return rootProjects();
		String sql = "select id, name, description "
				+ "from tbl_projects where f_project_folder = '"
				+ folder.id + "'";
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
			log.error("failed to getProjects", e);
		}
		return list;
	}
}
