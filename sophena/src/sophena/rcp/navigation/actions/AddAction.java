package sophena.rcp.navigation.actions;

import java.lang.reflect.Method;

import org.eclipse.jface.resource.ImageDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.rcp.Images;
import sophena.rcp.navigation.ConsumerElement;
import sophena.rcp.navigation.FolderElement;
import sophena.rcp.navigation.FolderType;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.ProducerElement;
import sophena.rcp.wizards.ConsumerWizard;
import sophena.rcp.wizards.ProducerWizard;

public class AddAction extends NavigationAction {

	private Logger log = LoggerFactory.getLogger(getClass());

	private NavigationElement elem;
	private Method handler;

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
	public ImageDescriptor getImageDescriptor() {
		return Images.ADD_16.des();
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

	@Handler(type = FolderElement.class,
			title = "Neuer Wärmeerzeuger",
			folderType = FolderType.PRODUCTION)
	private void producerOnFolder() {
		FolderElement e = (FolderElement) elem;
		ProducerWizard.open(e.getProject());
	}

	@Handler(type = ProducerElement.class,
			title = "Neuer Wärmeerzeuger")
	private void producerOnElement() {
		ProducerElement e = (ProducerElement) elem;
		ProducerWizard.open(e.getProject());
	}

	@Handler(type = FolderElement.class,
			title = "Neuer Wärmeabnehmer",
			folderType = FolderType.CONSUMPTION)
	private void consumerOnFolder() {
		FolderElement e = (FolderElement) elem;
		ConsumerWizard.open(e.getProject());
	}

	@Handler(type = ConsumerElement.class,
			title = "Neuer Wärmeabnehmer")
	private void consumerOnElement() {
		ConsumerElement e = (ConsumerElement) elem;
		ConsumerWizard.open(e.getProject());
	}

}
