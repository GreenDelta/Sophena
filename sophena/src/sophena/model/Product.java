package sophena.model;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_products")
public class Product extends AbstractProduct {

	/**
	 * Contains only a value for project-private products. Products from the
	 * product database are globally visible to all projects.
	 */
	@Column(name = "f_project")
	public String projectId;

	@Override
	public Product clone() {
		Product clone = new Product();
		clone.id = UUID.randomUUID().toString();
		clone.name = name;
		clone.description = description;
		clone.purchasePrice = purchasePrice;
		clone.url = url;
		clone.type = type;
		clone.group = group;
		clone.projectId = projectId;
		return clone;
	}
}
