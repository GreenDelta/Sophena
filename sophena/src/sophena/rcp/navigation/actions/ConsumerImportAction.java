package sophena.rcp.navigation.actions;

import sophena.io.excel.consumers.ConsumerReader;
import sophena.model.Consumer;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.SubFolderElement;
import sophena.rcp.navigation.SubFolderType;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.MsgBox;

import java.util.List;

public class ConsumerImportAction extends NavigationAction {

	private ProjectDescriptor project;

	public ConsumerImportAction() {
		setText("Import aus Excel ...");
		setImageDescriptor(Icon.EXCEL_16.des());
	}

	@Override
	public boolean accept(NavigationElement e) {
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
		if (r.hasError()) {
			MsgBox.error("Importfehler", r.error());
			return;
		}
		if (r.isEmpty()) {
			MsgBox.info(
					"Keine Abnehmer",
					"Es konnten keine Abnehmer aus der Datei gelesen werden.");
			return;
		}
		var consumers = r.consumers();
		var prefix = consumers.size() == 1
			? "Aus der Datei wird ein Abnehmer"
			: "Aus der Datei werden " + consumers.size() + " Abnehmer";
		var msg = prefix + " in das ausgewählte Projekt importiert.";
		if (MsgBox.ask("Importieren", msg)) {
			runImport(consumers);
		}
	}

	private void runImport(List<Consumer> consumers) {
		// TODO: not yet implemented
	}
}
