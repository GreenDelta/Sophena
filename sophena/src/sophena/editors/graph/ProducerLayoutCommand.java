package sophena.editors.graph;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;

import sophena.model.Producer;

public class ProducerLayoutCommand extends Command {

	private EditPart parent;
	private Producer producer;
	private int x;
	private int y;

	public ProducerLayoutCommand(EditPart parent) {
		this.parent = parent;
	}

	@Override
	public boolean canExecute() {
		return producer != null;
	}

	@Override
	public void execute() {
		producer.setX(x);
		producer.setY(y);
		parent.refresh();
	}

	public void setProducer(Producer producer) {
		this.producer = producer;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

}
