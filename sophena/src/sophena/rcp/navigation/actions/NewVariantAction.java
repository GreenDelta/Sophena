package sophena.rcp.navigation.actions;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import sophena.model.Project;
import sophena.rcp.Images;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.ProjectElement;
import sophena.rcp.utils.UI;

public class NewVariantAction extends NavigationAction {

	private ProjectElement elem;

	public NewVariantAction() {
		setText("#Neue Variante");
		setImageDescriptor(Images.NEW_PROJECT_16.des());
	}

	@Override
	public boolean accept(NavigationElement element) {
		if (!(element instanceof ProjectElement))
			return false;
		ProjectElement e = (ProjectElement) element;
		if (e.getProject() == null || e.getProject().isVariant())
			return false;
		this.elem = e;
		return true;
	}

	@Override
	public void run() {
		if(elem == null || elem.getProject() == null)
			return;
		InputDialog dialog = new InputDialog(UI.shell(), "#Neue Variante",
				"#Name der Variante", "#Neue Variante", this::checkName);
		if (dialog.open() == Window.OK) {
			String val = dialog.getValue();
			if(val == null)
				return;
			Project variant = elem.getProject().clone();

		}
	}

	private String checkName(String name) {
		if(name == null || name.trim().length() == 0)
			return "#Der Name darf nicht leer sein";
		Project p = elem.getProject();
		String n = name.trim();
		for(Project var : p.getVariants())  {
			if(n.equalsIgnoreCase(p.getName()))
				return "#Es existiert schon eine Variante mit diesem Namen";
		}
		return null;
	}
}
