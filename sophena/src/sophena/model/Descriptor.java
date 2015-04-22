package sophena.model;

import java.util.Objects;

public class Descriptor {

	private String id;
	private String name;
	private String description;
	private ModelType type;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ModelType getType() {
		return type;
	}

	public void setType(ModelType type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		if (id == null)
			return super.hashCode();
		else
			return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		if(obj == this)
			return true;
		if(!(obj instanceof Descriptor))
			return false;
		Descriptor other = (Descriptor)obj;
		return this.type == other.type && Objects.equals(this.id, other.id);
	}
}
