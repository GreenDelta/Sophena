package sophena.model.descriptors;

import sophena.model.ModelType;

public class ProjectDescriptor extends Descriptor {

	private boolean variant;

	@Override
	public ModelType getType() {
		return ModelType.PROJECT;
	}

	public void setVariant(boolean variant) {
		this.variant = variant;
	}

	public boolean isVariant() {
		return variant;
	}

}
