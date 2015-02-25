package sophena.rcp.navigation.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ProjectDao;
import sophena.rcp.App;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.navigation.ProjectElement;
import sophena.rcp.utils.MsgBox;

public class DeleteAction extends NavigationAction {

	private NavigationElement elem;

	public DeleteAction() {
		setText(M.Delete);
		setImageDescriptor(Images.DELETE_16.des());
	}

	@Override
	public boolean accept(NavigationElement element) {
		if (element instanceof ProjectElement) {
			elem = element;
			return true;
		}
		return false;
	}

	@Override
	public void run() {
		if (elem instanceof ProjectElement) {
			deleteProject();
		}
	}

	private void deleteProject() {
		boolean del = MsgBox.ask("#Projekt löschen?",
				"#Soll das Projekt wirklich endgültig gelöscht werden?");
		if (!del)
			return;
		try {
			ProjectElement e = (ProjectElement) elem;
			ProjectDao dao = new ProjectDao(App.getDb());
			dao.delete(e.getProject());
			Navigator.refresh();
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to delete project", e);
		}
	}

}
