package sophena.model.descriptors;

import sophena.model.ModelType;

public class ConsumerDescriptor extends Descriptor {

	public boolean disabled;

	@Override
	public ModelType getType() {
		return ModelType.CONSUMER;
	}

}
