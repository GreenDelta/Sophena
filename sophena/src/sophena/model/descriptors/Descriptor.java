package sophena.model.descriptors;

import sophena.model.ModelType;
import sophena.model.RootEntity;

public abstract class Descriptor extends RootEntity {

	public abstract ModelType getType();

	static void copyFields(Descriptor from, Descriptor to) {
		if (from == null || to == null)
			return;
		to.id = from.id;
		to.name = from.name;
		to.description = from.description;
	}
}
