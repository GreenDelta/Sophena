package sophena.model;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

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
	public Product copy() {
		var clone = new Product();
		AbstractProduct.copyFields(this, clone);
		clone.id = UUID.randomUUID().toString();
		clone.projectId = projectId;
		return clone;
	}
}
