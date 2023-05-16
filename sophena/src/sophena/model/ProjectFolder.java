package sophena.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * In Sophena, projects can be organized in folders. Sub-folders are currently
 * not supported.
 */
@Entity
@Table(name = "tbl_project_folders")
public class ProjectFolder extends RootEntity {
}
