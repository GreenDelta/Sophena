package sophena.model.descriptors;

import sophena.model.ModelType;

public class ProducerDescriptor extends Descriptor {

	public boolean disabled;
	public int rank;

	@Override
	public ModelType getType() {
		return ModelType.PRODUCER;
	}

	@Override
	public ProducerDescriptor copy() {
		var copy = new ProducerDescriptor();
		Descriptor.copyFields(this, copy);
		return copy;
	}

}
