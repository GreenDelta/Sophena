package sophena.editors.graph;

import java.util.UUID;

import org.eclipse.gef.requests.CreationFactory;

import sophena.model.Consumer;
import sophena.model.Facility;
import sophena.model.FacilityType;
import sophena.model.Producer;
import sophena.model.Pump;

public class FacilityFactory implements CreationFactory {

	private final FacilityType type;

	public FacilityFactory(FacilityType type) {
		this.type = type;
	}

	@Override
	public Object getNewObject() {
		Facility facility = make();
		facility.setId(UUID.randomUUID().toString());
		facility.setName(name());
		return facility;
	}

	private Facility make() {
		if (type == null)
			return null;
		switch (type) {
		case CONSUMER:
			return new Consumer();
		case PRODUCER:
			return new Producer();
		case PUMP:
			return new Pump();
		default:
			return null;
		}
	}

	private String name() {
		if (type == null)
			return null;
		switch (type) {
		case CONSUMER:
			return "@New consumer";
		case PRODUCER:
			return "@New producer";
		case PUMP:
			return "@New pump";
		default:
			return "@Unknown";
		}
	}

	@Override
	public Object getObjectType() {
		return type;
	}

}
