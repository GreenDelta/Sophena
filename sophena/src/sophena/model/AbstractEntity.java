package sophena.model;

import java.util.Objects;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * An abstract entity is basically just a thing that can be stored in a
 * database.
 */
@MappedSuperclass
public abstract class AbstractEntity implements Copyable<AbstractEntity> {

	@Id
	public String id;

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[id = " + id + "]";
	}

	@Override
	public int hashCode() {
		return id != null
			? id.hashCode()
			: super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!obj.getClass().equals(this.getClass()))
			return false;
		var other = (AbstractEntity) obj;
		return this.id != null
			&& other.id != null
			&& Objects.equals(this.id, other.id);
	}
}
