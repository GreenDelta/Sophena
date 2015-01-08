package sophena.editors.graph;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;

import sophena.model.Producer;
import sophena.model.Project;

public class ProducerCreationCommand extends Command {

	private EditPart parent;
	private Project project;
	private Producer producer;
	private int x;
	private int y;

	public ProducerCreationCommand(EditPart parent) {
		this.parent = parent;
	}

	public void setProject(Project project) {
		this.project = project;
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

	@Override
	public boolean canExecute() {
		return project != null && producer != null;
	}

	@Override
	public void execute() {
		producer.setX(x);
		producer.setY(y);
		project.getProducers().add(producer);
		parent.refresh();
	}

	@Override
	public boolean canUndo() {
		if (project == null || producer == null)
			return false;
		return project.getProducers().contains(producer);
	}

	@Override
	public void undo() {
		project.getProducers().remove(producer);
	}

}
