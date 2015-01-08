package sophena.editors.graph;

import org.eclipse.gef.requests.CreationFactory;

import sophena.model.Producer;

public class ProducerFactory implements CreationFactory {

	@Override
	public Object getNewObject() {
		Producer producer = new Producer();
		producer.setName("new producer");
		return producer;
	}

	@Override
	public Object getObjectType() {
		return Producer.class;
	}

}
