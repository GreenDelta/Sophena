package sophena.model;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * In Sophena, projects can be organized in folders. Sub-folders are currently
 * not supported.
 */
@Entity
@Table(name = "tbl_project_folders")
public class ProjectFolder extends RootEntity {
}
