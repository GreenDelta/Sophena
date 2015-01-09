package sophena.editors.graph;

import java.util.UUID;

import org.eclipse.gef.commands.Command;

import sophena.model.Facility;
import sophena.model.Pipe;
import sophena.model.Project;

public class PipeCreationCommand extends Command {

	private ProjectPart projectPart;
	private Facility provider;
	private Facility recipient;

	public PipeCreationCommand(ProjectPart projectPart) {
		this.projectPart = projectPart;
	}

	public void setProvider(Facility provider) {
		this.provider = provider;
	}

	public void setRecipient(Facility recipient) {
		this.recipient = recipient;
	}

	@Override
	public boolean canExecute() {
		if (provider == null || recipient == null)
			return false;
		if (provider.equals(recipient))
			return false;
		// TODO: check if there is already a pipe with the same provider and
		// recipient
		return true;
	}

	@Override
	public void execute() {
		if (provider == null || recipient == null)
			return;
		Pipe pipe = new Pipe();
		pipe.setId(UUID.randomUUID().toString());
		pipe.setProviderId(provider.getId());
		pipe.setRecipientId(recipient.getId());
		Project project = (Project) projectPart.getModel();
		project.getPipes().add(pipe);
		projectPart.refresh();
	}
}
