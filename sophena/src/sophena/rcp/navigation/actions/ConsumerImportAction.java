package sophena.rcp.navigation.actions;

import java.util.List;

import sophena.db.daos.ProjectDao;
import sophena.io.excel.consumers.ConsumerReader;
import sophena.model.Consumer;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.navigation.SubFolderElement;
import sophena.rcp.navigation.SubFolderType;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.MsgBox;

public class ConsumerImportAction extends NavigationAction {

	private ProjectDescriptor project;

	public ConsumerImportAction() {
		setText("Import aus Excel ...");
		setImageDescriptor(Icon.EXCEL_16.des());
	}

	@Override
	public boolean accept(List<NavigationElement> elements) {
		if (elements == null || elements.size() != 1)
			return false;
		var e = elements.getFirst();
		if (!(e instanceof SubFolderElement folder))
			return false;
		if (folder.getType() != SubFolderType.CONSUMPTION)
			return false;
		this.project = folder.getProject();
		return true;
	}

	@Override
	public void run() {
		var file = FileChooser.open("*.xlsx");
		if (file == null)
			return;
		var r = ConsumerReader.of(file, App.getDb()).read();
		var err = "Es konnten keine Abnehmer aus der Datei gelesen werden.";
		if (r.isError()) {
			MsgBox.error("Importfehler", r.message().orElse(err));
			return;
		}
		var consumers = r.get();
		if (consumers == null || consumers.isEmpty()) {
			MsgBox.info("Keine Abnehmer", err);
			return;
		}
		var prefix = consumers.size() == 1
				? "Aus der Datei wird ein Abnehmer"
				: "Aus der Datei werden " + consumers.size() + " Abnehmer";
		var msg = prefix + " in das ausgewählte Projekt importiert.";
		if (MsgBox.ask("Importieren", msg)) {
			runImport(consumers);
		}
	}

	private void runImport(List<Consumer> consumers) {
		var dao = new ProjectDao(App.getDb());
		var project = dao.get(this.project.id);
		if (project == null)
			return;
		project.consumers.addAll(consumers);
		dao.update(project);
		Navigator.refresh();
	}
}
