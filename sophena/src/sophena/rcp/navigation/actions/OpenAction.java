package sophena.rcp.navigation.actions;

import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.editors.consumers.ConsumerEditor;
import sophena.rcp.editors.costs.CostEditor;
import sophena.rcp.editors.heatnets.HeatNetEditor;
import sophena.rcp.editors.producers.ProducerEditor;
import sophena.rcp.editors.projects.ProjectEditor;
import sophena.rcp.navigation.CleaningElement;
import sophena.rcp.navigation.ConsumerElement;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.ProducerElement;
import sophena.rcp.navigation.ProjectElement;
import sophena.rcp.navigation.SubFolderElement;
import sophena.rcp.navigation.SubFolderType;

public class OpenAction extends NavigationAction {

	private final Logger log = LoggerFactory.getLogger(getClass());

	private NavigationElement elem;
	private Method handler;

	public OpenAction() {
		setText(M.Open);
		setImageDescriptor(Icon.OPEN_16.des());
	}

	@Override
	public boolean accept(List<NavigationElement> elements) {
		if (elements == null || elements.isEmpty())
			return false;
		var element = elements.getFirst();
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
			log.error("failed to call {} with {}", handler, elem, e);
		}
	}

	@Handler(type = ProjectElement.class, title = "Öffne Projektinformationen")
	private void openProject() {
		ProjectElement e = (ProjectElement) elem;
		ProjectEditor.open(e.content);
	}

	@Handler(type = ConsumerElement.class, title = "Öffne Wärmeabnehmer")
	private void openConsumer() {
		ConsumerElement e = (ConsumerElement) elem;
		ConsumerEditor.open(e.getProject(), e.content);
	}

	@Handler(type = ProducerElement.class, title = "Öffne Wärmeerzeuger")
	private void openProducer() {
		ProducerElement e = (ProducerElement) elem;
		ProducerEditor.open(e.getProject(), e.content);
	}

	@Handler(type = CleaningElement.class, title = "Öffne Rauchgasreinigung")
	private void openCleaning() {
		CleaningElement e = (CleaningElement) elem;
		Cleanings.open(e);
	}

	@Handler(type = SubFolderElement.class,
		title = "Öffne Wärmenetzinformationen",
		folderType = SubFolderType.DISTRIBUTION)
	private void openDistributionInfo() {
		SubFolderElement e = (SubFolderElement) elem;
		HeatNetEditor.open(e.getProject());
	}

	@Handler(type = SubFolderElement.class,
		title = "Öffne Investitionen",
		folderType = SubFolderType.COSTS)
	private void openCostEditor() {
		SubFolderElement e = (SubFolderElement) elem;
		CostEditor.open(e.getProject());
	}

}
