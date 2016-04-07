package sophena.rcp.navigation.actions;

import java.io.File;

import sophena.db.daos.ProjectDao;
import sophena.io.datapack.DataPack;
import sophena.io.datapack.Export;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.ProjectElement;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.Log;
import sophena.rcp.utils.Rcp;

public class ExportAction extends NavigationAction {

	private ProjectElement elem;

	public ExportAction() {
		setText("Projekt exportieren");
		setImageDescriptor(Icon.EXPORT_16.des());
	}

	@Override
	public boolean accept(NavigationElement elem) {
		if (elem instanceof ProjectElement) {
			this.elem = (ProjectElement) elem;
			return true;
		}
		return false;
	}

	@Override
	public void run() {
		if (elem == null || elem.getDescriptor() == null)
			return;
		ProjectDescriptor d = elem.getDescriptor();
		File file = FileChooser.saveFile(d.name + ".sophena", "*.sophena");
		if (file == null)
			return;
		Rcp.run("Exportiere Projekt ...", () -> tryExport(d, file));
	}

	private void tryExport(ProjectDescriptor d, File file) {
		try {
			if (file.exists())
				file.delete();
			ProjectDao dao = new ProjectDao(App.getDb());
			Project project = dao.get(d.id);
			DataPack pack = new DataPack(file);
			Export export = new Export(pack);
			export.write(project);
			pack.close();
		} catch (Exception e) {
			Log.error(this, "project export failed", e);
		}
	}

}
