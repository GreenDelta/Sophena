package sophena.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * In Sophena, projects can be organized in folders. Sub-folders are currently
 * not supported.
 */
@Entity
@Table(name = "tbl_project_folders")
public class ProjectFolder extends RootEntity {

	@Override
	public ProjectFolder copy() {
		var copy = new ProjectFolder();
		copy.id = UUID.randomUUID().toString();
		copy.name = name;
		copy.description = description;
		return copy;
	}
}
