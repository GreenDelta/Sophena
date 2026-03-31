package sophena.rcp.navigation.actions.producers;

import java.util.List;

import sophena.db.daos.ProducerDao;
import sophena.db.daos.ProjectDao;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.SubFolderElement;
import sophena.rcp.navigation.SubFolderType;
import sophena.rcp.navigation.actions.NavigationAction;
import sophena.rcp.utils.MsgBox;
import sophena.utils.Num;

/// An action for estimating the peak-load boiler of a project.
public class PeakBoilerAction extends NavigationAction {

	private ProjectDescriptor project;

	public PeakBoilerAction() {
		setText("Spitzenlastkessel abschätzen");
		setImageDescriptor(Icon.BOILER_16.des());
	}

	@Override
	public boolean accept(List<NavigationElement> list) {
		if (list == null || list.size() != 1)
			return false;
		if (!(list.getFirst() instanceof SubFolderElement folder))
			return false;
		if (folder.getType() != SubFolderType.PRODUCTION)
			return false;

		var project = folder.getProject();
		if (project == null)
			return false;

		var dao = new ProducerDao(App.getDb());
		if (dao.getDescriptors(project).isEmpty())
			return false;

		this.project = project;
		return true;
	}

	@Override
	public void run() {
		if (project == null)
			return;

		var project = new ProjectDao(App.getDb()).get(this.project.id);
		if (project == null)
			return;

		double demand = PeakDemandEstimator.estimate(project);
		if (demand < 0.1) {
			MsgBox.info("Kein weiterer Spitzenlastkessel erforderlich",
				"Aus dem Abgleich des Wärmebedarfs mit der Erzeugerleistung ergibt " +
					"sich kein Bedarf für einen zusätzlichen Spitzenlastkessel.");
			return;
		}
		PeakBoilerDialog.open(project, demand);
	}
}
