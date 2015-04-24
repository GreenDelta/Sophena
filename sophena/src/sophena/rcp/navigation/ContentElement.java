package sophena.rcp.navigation;

import java.util.Objects;
import sophena.model.descriptors.Descriptor;
import sophena.rcp.utils.Strings;

abstract class ContentElement<T extends Descriptor> implements
		NavigationElement {

	private T descriptor;
	private NavigationElement parent;

	public ContentElement(NavigationElement parent, T descriptor) {
		this.descriptor = descriptor;
		this.parent = parent;
	}

	@Override
	public NavigationElement getParent() {
		return parent;
	}

	public T getDescriptor() {
		return descriptor;
	}

	public void setDescriptor(T descriptor) {
		this.descriptor = descriptor;
	}

	@Override
	public String getLabel() {
		T descriptor = getDescriptor();
		if (descriptor == null)
			return null;
		else
			return descriptor.getName();
	}

	@Override
	public int compareTo(NavigationElement other) {
		if (other == null)
			return 1;
		if (!(other instanceof ContentElement))
			return 1;
		ContentElement<?> otherElem = ContentElement.class.cast(other);
		Descriptor otherDes = otherElem.descriptor;
		if (this.descriptor == null || otherDes == null)
			return 0;
		else
			return Strings.compare(this.descriptor.getName(),
					otherDes.getName());
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!obj.getClass().equals(this.getClass()))
			return false;
		ContentElement<?> other = ContentElement.class.cast(obj);
		return Objects.equals(this.getDescriptor(), other.getDescriptor());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getDescriptor());
	}

}
