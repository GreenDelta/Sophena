package sophena.model.descriptors;

import sophena.model.ModelType;

public class CleaningDescriptor extends Descriptor {

	@Override
	public ModelType getType() {
		return ModelType.FLUE_GAS_CLEANING;
	}

	@Override
	public CleaningDescriptor copy() {
		var copy = new CleaningDescriptor();
		Descriptor.copyFields(this, copy);
		return copy;
	}

}
