package sophena.rcp.navigation;

import java.util.Objects;

import sophena.model.ModelType;
import sophena.model.RootEntity;
import sophena.model.descriptors.Descriptor;
import sophena.utils.Strings;

abstract class ContentElement<T extends RootEntity> implements
		NavigationElement {

	public T content;
	private NavigationElement parent;

	public ContentElement(NavigationElement parent, T descriptor) {
		this.content = descriptor;
		this.parent = parent;
	}

	@Override
	public NavigationElement getParent() {
		return parent;
	}

	@Override
	public String getLabel() {
		return content == null ? null : content.name;
	}

	@Override
	public int compareTo(NavigationElement other) {
		if (other == null)
			return 1;
		if (!(other instanceof ContentElement))
			return 1;
		ContentElement<?> otherElem = ContentElement.class.cast(other);
		RootEntity otherDes = otherElem.content;
		if (this.content == null || otherDes == null)
			return 0;
		ModelType type = modelType(this);
		ModelType otherType = modelType(otherElem);
		// sort producers and flue gas cleanings by type
		if (type != null && otherType != null && type != otherType)
			return otherType.ordinal() - type.ordinal();
		return Strings.compare(this.getLabel(), otherElem.getLabel());
	}

	private ModelType modelType(ContentElement<?> elem) {
		if (elem == null || elem.content == null)
			return null;
		RootEntity content = elem.content;
		if (content instanceof Descriptor)
			return ((Descriptor) content).getType();
		return ModelType.forModelClass(content.getClass());
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
		return Objects.equals(this.content, other.content);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(content);
	}

}
