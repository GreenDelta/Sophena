package sophena.model.descriptors;

import sophena.model.ModelType;

public class ProducerDescriptor extends Descriptor {

	private boolean disabled;

	@Override
	public ModelType getType() {
		return ModelType.PRODUCER;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

}
