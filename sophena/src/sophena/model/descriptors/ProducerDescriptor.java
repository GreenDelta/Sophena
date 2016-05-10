package sophena.model.descriptors;

import sophena.model.ModelType;

public class ProducerDescriptor extends Descriptor {

	public boolean disabled;
	public int rank;

	@Override
	public ModelType getType() {
		return ModelType.PRODUCER;
	}

}
