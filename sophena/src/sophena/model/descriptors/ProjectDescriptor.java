package sophena.model.descriptors;

import sophena.model.ModelType;

public class ProjectDescriptor extends Descriptor {

	@Override
	public ModelType getType() {
		return ModelType.PROJECT;
	}

	@Override
	public ProjectDescriptor copy() {
		var copy = new ProjectDescriptor();
		Descriptor.copyFields(this, copy);
		return copy;
	}

}
