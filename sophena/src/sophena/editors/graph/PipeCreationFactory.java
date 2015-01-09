package sophena.editors.graph;

import org.eclipse.gef.requests.CreationFactory;

import sophena.model.Pipe;

public class PipeCreationFactory implements CreationFactory {

	@Override
	public Object getNewObject() {
		return null;
	}

	@Override
	public Object getObjectType() {
		return Pipe.class;
	}

}
