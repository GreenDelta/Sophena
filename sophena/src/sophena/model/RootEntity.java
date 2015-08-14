package sophena.model;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class RootEntity extends AbstractEntity {

	@Column(name = "name")
	public String name;

	@Column(name = "description")
	public String description;

}
