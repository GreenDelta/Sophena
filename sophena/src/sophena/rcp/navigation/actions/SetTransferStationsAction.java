package sophena.rcp.navigation.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.util.Strings;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import sophena.db.daos.ProjectDao;
import sophena.model.Manufacturer;
import sophena.model.Project;
import sophena.model.TransferStation;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.navigation.NavigationElement;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.navigation.SubFolderElement;
import sophena.rcp.navigation.SubFolderType;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.UI;

public class SetTransferStationsAction extends NavigationAction {

	private ProjectDescriptor project;

	public SetTransferStationsAction() {
		setText("Übergabestationen setzen");
		setImageDescriptor(Icon.CONSUMER_16.des());
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
		if (project == null)
			return;
		var dao = new ProjectDao(App.getDb());
		var p = dao.get(project.id);
		if (p == null)
			return;
		var wizard = new AssignmentWizard(p);
		var dialog = new WizardDialog(UI.shell(), wizard);
		if (dialog.open() == Window.OK) {
			Navigator.refresh();
		}
	}

	private static class AssignmentWizard extends Wizard {

		private final Project project;
		private Page page;

		AssignmentWizard(Project project) {
			this.project = project;
			setWindowTitle("Übergabestationen setzen");
		}

		@Override
		public void addPages() {
			page = new Page();
			addPage(page);
		}

		@Override
		public boolean performFinish() {
			var manufacturer = page.manufacturer;
			var productLine = page.productLine;
			var overwrite = page.overwrite;

			var stations = new ArrayList<TransferStation>();
			for (var s : App.getDb().getAll(TransferStation.class)) {
				if (Objects.equals(s.manufacturer, manufacturer)
					&& Objects.equals(s.productLine, productLine)) {
					stations.add(s);
				}
			}

			if (stations.isEmpty()) {
				MsgBox.error("Keine Übergabestationen gefunden",
					"Es wurden für die ausgewählte Produktlinie des " +
						"Herstellers keine Übergabestationen gefunden.");
				return false;
			}

			// TODO: apply stations to consumers
			// The logic for which station to assign to which consumer
			// will be added later

			var dao = new ProjectDao(App.getDb());
			dao.update(project);
			return true;
		}

		private static class Page extends WizardPage {

			private final List<Manufacturer> manufacturers;
			private final List<TransferStation> stations;
			private Manufacturer manufacturer;
			private String productLine;
			private boolean overwrite;

			Page() {
				super("SetTransferStationsPage", "Übergabestationen setzen", null);
				setMessage("Wählen Sie einen Hersteller und eine Produktlinie aus.");
				var db = App.getDb();
				this.stations = db.getAll(TransferStation.class);
				Sorters.byName(stations);
				this.manufacturers = new ArrayList<>();
				for (var s : stations) {
					if (!manufacturers.contains(s.manufacturer)) {
						manufacturers.add(s.manufacturer);
					}
				}
				Sorters.byName(manufacturers);
			}

			@Override
			public void createControl(Composite parent) {
				var root = new Composite(parent, SWT.NONE);
				setControl(root);
				UI.gridLayout(root, 1, 5, 5);
				var comp = UI.formComposite(root);
				UI.gridData(comp, true, false);

				var manCombo = UI.formCombo(comp, "Hersteller");
				var manItems = manufacturers.stream()
					.map(m -> m != null ? m.name : "")
					.toArray(String[]::new);
				manCombo.setItems(manItems);
				var lineCombo = UI.formCombo(comp, "Produktlinie");

				Controls.onSelect(manCombo, $ -> {
					int i = manCombo.getSelectionIndex();
					manufacturer = manufacturers.get(i);
					var pls = productLinesOf(manufacturer);
					lineCombo.setItems(pls);
					productLine = null;
					validate();
				});

				Controls.onSelect(lineCombo, $ -> {
					productLine = lineCombo.getItem(lineCombo.getSelectionIndex());
					validate();
				});

				UI.filler(comp);
				var check = new Button(comp, SWT.CHECK);
				check.setText("Bestehende Übergabestationen überschreiben");
				check.setSelection(overwrite);
				Controls.onSelect(check, $ -> overwrite = check.getSelection());

				validate();
			}

			private String[] productLinesOf(Manufacturer manufacturer) {
				return stations
					.stream()
					.filter(s -> Objects.equals(s.manufacturer, manufacturer))
					.map(s -> s.productLine)
					.distinct()
					.sorted()
					.toArray(String[]::new);
			}

			private void validate() {
				setPageComplete(Strings.isNotBlank(productLine));
			}
		}
	}
}

