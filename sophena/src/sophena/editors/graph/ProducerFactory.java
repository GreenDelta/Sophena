package sophena.editors.graph;

import java.util.UUID;
import org.eclipse.gef.requests.CreationFactory;

import sophena.model.Producer;

public class ProducerFactory implements CreationFactory {

	@Override
	public Object getNewObject() {
		Producer producer = new Producer();
		producer.setId(UUID.randomUUID().toString());
		producer.setName("new producer");
		return producer;
	}

	@Override
	public Object getObjectType() {
		return Producer.class;
	}

}
