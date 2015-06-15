package sophena.model.descriptors;

import sophena.model.ModelType;

public class ConsumerDescriptor extends Descriptor {

	private boolean disabled;

	@Override
	public ModelType getType() {
		return ModelType.CONSUMER;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

}
