package sophena.rcp.navigation.actions;

import org.eclipse.jface.action.Action;

import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.wizards.ProjectWizard;

public class NewProjectAction extends Action {

	public NewProjectAction() {
		setText(M.NewProject);
		setImageDescriptor(Images.NEW_PROJECT_16.des());
	}

	@Override
	public void run() {
		ProjectWizard.open();
	}

}
