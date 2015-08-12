package sophena.rcp.navigation.actions;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ProjectDao;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.Images;
import sophena.rcp.navigation.FolderElement;
import sophena.rcp.navigation.FolderType;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.navigation.ProjectElement;
import sophena.rcp.utils.UI;

public class NewVariantAction extends NavigationAction {

	private ProjectDescriptor descriptor;

	public NewVariantAction() {
		setText("Neue Variante");
		setImageDescriptor(Images.NEW_PROJECT_16.des());
	}

	@Override
	public boolean accept(NavigationElement element) {
		if (element instanceof ProjectElement)
			return acceptProjectElement((ProjectElement) element);
		if (element instanceof FolderElement)
			return acceptStructureElement((FolderElement) element);
		else
			return false;
	}

	private boolean acceptStructureElement(FolderElement element) {
		FolderElement e = element;
		if (e.getType() != FolderType.VARIANTS)
			return false;
		ProjectDescriptor p = e.getProject();
		if (p == null || p.isVariant())
			return false;
		this.descriptor = p;
		return true;
	}

	private boolean acceptProjectElement(ProjectElement element) {
		ProjectElement e = element;
		ProjectDescriptor p = e.getDescriptor();
		if (p == null || p.isVariant())
			return false;
		this.descriptor = p;
		return true;
	}

	@Override
	public void run() {
		if (descriptor == null)
			return;
		InputDialog dialog = new InputDialog(UI.shell(), "Neue Variante",
				"Name der Variante", "Neue Variante", this::checkName);
		if (dialog.open() == Window.OK) {
			String val = dialog.getValue();
			if (val == null)
				return;
			createVariant(val);
		}
	}

	private void createVariant(String val) {
		try {
			ProjectDao dao = new ProjectDao(App.getDb());
			Project p = dao.get(descriptor.id);
			Project variant = p.clone();
			variant.setName(val);
			variant.setVariant(true);
			variant.getVariants().clear();
			p.getVariants().add(variant);
			dao.update(p);
			Navigator.refresh();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to save project variant", e);
		}
	}

	private String checkName(String name) {
		if (name == null || name.trim().length() == 0)
			return "Der Name darf nicht leer sein";
		String n = name.trim();
		// TODO: search in database
		// for (Project var : descriptor.getVariants()) {
		// if (n.equalsIgnoreCase(var.getName()))
		// return "Es existiert schon eine Variante mit diesem Namen";
		// }
		return null;
	}
}
