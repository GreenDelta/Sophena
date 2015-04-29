package sophena.rcp.navigation.actions;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.editors.consumers.ConsumerEditor;
import sophena.rcp.editors.heatnets.HeatNetEditor;
import sophena.rcp.editors.producers.ProducerEditor;
import sophena.rcp.editors.projects.ProjectEditor;
import sophena.rcp.navigation.ConsumerElement;
import sophena.rcp.navigation.FolderElement;
import sophena.rcp.navigation.FolderType;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.ProducerElement;
import sophena.rcp.navigation.ProjectElement;

public class OpenAction extends NavigationAction {

	private Logger log = LoggerFactory.getLogger(getClass());

	private NavigationElement elem;
	private Method handler;

	public OpenAction() {
		setText(M.Open);
		setImageDescriptor(Images.OPEN_16.des());
	}

	@Override
	public boolean accept(NavigationElement element) {
		handler = Handlers.find(element, this);
		if (handler == null) {
			elem = null;
			return false;
		} else {
			elem = element;
			return true;
		}
	}

	@Override
	public void run() {
		try {
			log.trace("call {} with {}", handler, elem);
			handler.invoke(this);
		} catch (Exception e) {
			log.error("failed to call " + handler + " with " + elem, e);
		}
	}

	@Handler(type = ProjectElement.class,
			title = "Öffne Projektinformationen")
	private void openProject() {
		ProjectElement e = (ProjectElement) elem;
		ProjectEditor.open(e.getDescriptor());
	}

	@Handler(type = ConsumerElement.class,
			title = "Öffne Wärmeabnehmer")
	private void openConsumer() {
		ConsumerElement e = (ConsumerElement) elem;
		ConsumerEditor.open(e.getProject(), e.getDescriptor());
	}

	@Handler(type = ProducerElement.class,
			title = "Öffne Wärmeerzeuger")
	private void openProducer() {
		ProducerElement e = (ProducerElement) elem;
		ProducerEditor.open(e.getProject(), e.getDescriptor());
	}

	@Handler(type = FolderElement.class,
			title = "Öffne Wärmenetzinformationen",
			folderType = FolderType.DISTRIBUTION)
	private void openDistributionInfo() {
		FolderElement e = (FolderElement) elem;
		HeatNetEditor.open(e.getProject());
	}

}
