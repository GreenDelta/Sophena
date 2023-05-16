package sophena.model;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

/**
 * An abstract entity is basically just a thing that can be stored in a
 * database.
 */
@MappedSuperclass
public abstract class AbstractEntity {

	@Id
	public String id;

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[id = " + id + "]";
	}

	@Override
	public int hashCode() {
		if (id == null)
			return super.hashCode();
		else
			return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!obj.getClass().equals(this.getClass()))
			return false;
		AbstractEntity other = (AbstractEntity) obj;
		if (this.id == null || other.id == null)
			return false;
		else
			return this.id.equals(other.id);
	}
}
