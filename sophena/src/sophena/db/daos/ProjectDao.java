package sophena.db.daos;

import sophena.db.Database;
import sophena.model.Project;

public class ProjectDao extends RootEntityDao<Project> {

	public ProjectDao(Database db) {
		super(Project.class, db);
	}

}
