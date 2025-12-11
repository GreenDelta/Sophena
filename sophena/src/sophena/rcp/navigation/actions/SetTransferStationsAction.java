package sophena.rcp.navigation.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import sophena.db.daos.Dao;
import sophena.db.daos.ProjectDao;
import sophena.db.daos.RootEntityDao;
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
import sophena.rcp.utils.EntityCombo;
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
		var wizard = new SetTransferStationsWizard(p);
		var dialog = new WizardDialog(UI.shell(), wizard);
		if (dialog.open() == Window.OK) {
			Navigator.refresh();
		}
	}

	private static class SetTransferStationsWizard extends Wizard {

		private final Project project;
		private final List<TransferStation> allStations;
		private Page page;

		SetTransferStationsWizard(Project project) {
			this.project = project;
			setWindowTitle("Übergabestationen setzen");
			var dao = new RootEntityDao<>(TransferStation.class, App.getDb());
			this.allStations = dao.getAll();
		}

		@Override
		public void addPages() {
			page = new Page();
			addPage(page);
		}

		@Override
		public boolean performFinish() {
			var manufacturer = page.getSelectedManufacturer();
			var productLine = page.getSelectedProductLine();
			var overwriteExisting = page.isOverwriteExisting();

			// filter stations by manufacturer and product line
			var stations = allStations.stream()
					.filter(s -> s.manufacturer != null
							&& s.manufacturer.id.equals(manufacturer.id))
					.filter(s -> productLine == null
							|| productLine.isEmpty()
							|| productLine.equals(s.productLine))
					.toList();

			// TODO: apply stations to consumers
			// The logic for which station to assign to which consumer
			// will be added later

			var dao = new ProjectDao(App.getDb());
			dao.update(project);
			return true;
		}

		private class Page extends WizardPage {

			private EntityCombo<Manufacturer> manufacturerCombo;
			private Combo productLineCombo;
			private Button overwriteCheck;
			private List<String> productLines = new ArrayList<>();

			Page() {
				super("SetTransferStationsPage", "Übergabestationen setzen", null);
				setMessage("Wählen Sie einen Hersteller und eine Produktlinie aus.");
			}

			@Override
			public void createControl(Composite parent) {
				var root = new Composite(parent, SWT.NONE);
				setControl(root);
				UI.gridLayout(root, 1, 5, 5);

				var comp = UI.formComposite(root);
				UI.gridData(comp, true, false);

				createManufacturerCombo(comp);
				createProductLineCombo(comp);
				createOverwriteCheck(comp);

				updateProductLines();
				validate();
			}

			private void createManufacturerCombo(Composite comp) {
				manufacturerCombo = new EntityCombo<>();
				manufacturerCombo.create("Hersteller", comp);

				// get manufacturers that have transfer stations
				var manufacturerIds = allStations.stream()
						.filter(s -> s.manufacturer != null)
						.map(s -> s.manufacturer.id)
						.collect(Collectors.toSet());

				var manufacturerDao = new Dao<>(Manufacturer.class, App.getDb());
				var manufacturers = manufacturerDao.getAll().stream()
						.filter(m -> manufacturerIds.contains(m.id))
						.collect(Collectors.toList());
				Sorters.byName(manufacturers);

				manufacturerCombo.setInput(manufacturers);
				if (!manufacturers.isEmpty()) {
					manufacturerCombo.select(manufacturers.getFirst());
				}

				manufacturerCombo.onSelect(m -> {
					updateProductLines();
					validate();
				});
			}

			private void createProductLineCombo(Composite comp) {
				UI.formLabel(comp, "Produktlinie");
				productLineCombo = new Combo(comp, SWT.READ_ONLY);
				UI.gridData(productLineCombo, true, false);
				Controls.onSelect(productLineCombo, e -> validate());
			}

			private void createOverwriteCheck(Composite comp) {
				UI.filler(comp);
				overwriteCheck = new Button(comp, SWT.CHECK);
				overwriteCheck.setText("Bestehende Übergabestationen überschreiben");
				overwriteCheck.setSelection(false);
			}

			private void updateProductLines() {
				productLines.clear();
				productLineCombo.removeAll();

				var manufacturer = manufacturerCombo.getSelected();
				if (manufacturer == null)
					return;

				// get distinct product lines for the selected manufacturer
				productLines = allStations.stream()
						.filter(s -> s.manufacturer != null
								&& s.manufacturer.id.equals(manufacturer.id))
						.map(s -> s.productLine)
						.filter(pl -> pl != null && !pl.isEmpty())
						.distinct()
						.sorted()
						.collect(Collectors.toList());

				// add empty option for "all product lines"
				productLineCombo.add("(Alle Produktlinien)");
				for (var pl : productLines) {
					productLineCombo.add(pl);
				}
				productLineCombo.select(0);
			}

			private void validate() {
				var manufacturer = manufacturerCombo.getSelected();
				if (manufacturer == null) {
					setErrorMessage("Bitte wählen Sie einen Hersteller aus.");
					setPageComplete(false);
					return;
				}

				setErrorMessage(null);
				setPageComplete(true);
			}

			Manufacturer getSelectedManufacturer() {
				return manufacturerCombo.getSelected();
			}

			String getSelectedProductLine() {
				int idx = productLineCombo.getSelectionIndex();
				if (idx <= 0) {
					return null; // "all product lines" selected
				}
				return productLines.get(idx - 1);
			}

			boolean isOverwriteExisting() {
				return overwriteCheck.getSelection();
			}
		}
	}
}

