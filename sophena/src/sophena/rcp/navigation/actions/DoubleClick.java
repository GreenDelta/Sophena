package sophena.rcp.navigation.actions;

import java.lang.reflect.Method;

import org.eclipse.jface.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.rcp.editors.consumers.ConsumerEditor;
import sophena.rcp.editors.costs.CostEditor;
import sophena.rcp.editors.heatnets.HeatNetEditor;
import sophena.rcp.editors.producers.ProducerEditor;
import sophena.rcp.editors.projects.ProjectEditor;
import sophena.rcp.editors.results.single.ResultEditor;
import sophena.rcp.navigation.CleaningElement;
import sophena.rcp.navigation.ConsumerElement;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.ProducerElement;
import sophena.rcp.navigation.ProjectElement;
import sophena.rcp.navigation.SubFolderElement;
import sophena.rcp.navigation.SubFolderType;

public class DoubleClick extends Action {

	private NavigationElement elem;

	private DoubleClick(NavigationElement elem) {
		this.elem = elem;
	}

	public static void handleOn(NavigationElement elem) {
		new DoubleClick(elem).handle();
	}

	private void handle() {
		Method m = Handlers.find(elem, this);
		if (m == null)
			return;
		Logger log = LoggerFactory.getLogger(getClass());
		try {
			log.trace("call {} with {}", m, elem);
			m.invoke(this);
		} catch (Exception e) {
			log.error("failed to call " + m + " with " + elem, e);
		}
	}

	@Handler(type = ProjectElement.class, title = "Open project")
	private void openProject() {
		ProjectElement e = (ProjectElement) elem;
		ProjectEditor.open(e.content);
	}

	@Handler(type = ConsumerElement.class, title = "Open consumer")
	private void openConsumer() {
		ConsumerElement e = (ConsumerElement) elem;
		ConsumerEditor.open(e.getProject(), e.content);
	}

	@Handler(type = ProducerElement.class, title = "Open producer")
	private void openProducer() {
		ProducerElement e = (ProducerElement) elem;
		ProducerEditor.open(e.getProject(), e.content);
	}

	@Handler(type = CleaningElement.class, title = "Open cleaning")
	private void openCleaning() {
		CleaningElement e = (CleaningElement) elem;
		Cleanings.open(e);
	}

	@Handler(type = SubFolderElement.class, title = "Open heat distribution", folderType = SubFolderType.DISTRIBUTION)
	private void openDistribution() {
		SubFolderElement e = (SubFolderElement) elem;
		HeatNetEditor.open(e.getProject());
	}

	@Handler(type = SubFolderElement.class, title = "Open energy results", folderType = SubFolderType.RESULTS)
	private void openEnergyResults() {
		SubFolderElement e = (SubFolderElement) elem;
		ResultEditor.open(e.getProject());
	}

	@Handler(type = SubFolderElement.class, title = "Öffne Kosten", folderType = SubFolderType.COSTS)
	private void openCostEditor() {
		SubFolderElement e = (SubFolderElement) elem;
		CostEditor.open(e.getProject());
	}
}
