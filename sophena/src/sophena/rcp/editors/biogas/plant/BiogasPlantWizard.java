package sophena.rcp.editors.biogas.plant;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sophena.model.Boiler;
import sophena.model.FuelGroup;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.model.biogas.BiogasPlant;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Num;

public class BiogasPlantWizard extends Wizard {

	private final BiogasPlant plant;
	private Page page;

	public static Optional<BiogasPlant> open() {

		// find the product group
		ProductGroup group = null;
		for (var g : App.getDb().getAll(ProductGroup.class)) {
			if (g.type == ProductType.COGENERATION_PLANT
					&& g.fuelGroup == FuelGroup.BIOGAS) {
				group = g;
				break;
			}
		}
		if (group == null) {
			MsgBox.error("Produktgruppe Biogas-BHKW nicht gefunden",
					"Die Produktgruppe Biogas-BHKW wurde nicht in der Datenbank gefunden");
			return Optional.empty();
		}

		var plant = new BiogasPlant();
		plant.id = UUID.randomUUID().toString();
		plant.productGroup = group;

		var wizard = new BiogasPlantWizard(plant);
		wizard.setWindowTitle("Neue Biogasanlage");
		var dialog = new WizardDialog(UI.shell(), wizard);
		dialog.setPageSize(150, 450);
		return dialog.open() == Window.OK
				? Optional.of(wizard.plant)
				: Optional.empty();
	}

	private BiogasPlantWizard(BiogasPlant plant) {
		super();
		this.plant = plant;
	}

	@Override
	public void addPages() {
		page = new Page(plant.productGroup);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		page.update(plant);
		App.getDb().insert(plant);
		return true;
	}

	private static class Page extends WizardPage {

		private final ProductGroup group;
		private Text nameText;
		private boolean nameEdited;
		private TableViewer table;
		private Text powerFilter;

		private Page(ProductGroup group) {
			super("BiogasPlantWizardPage", "Neue Biogasanlage", null);
			setMessage(" ");
			this.group = group;
		}

		private void update(BiogasPlant plant) {
			plant.name = nameText.getText();
			plant.product = Viewers.getFirstSelected(table);
			plant.ratedPower = Texts.getDouble(powerFilter);
			if (plant.ratedPower == 0 && plant.product != null) {
				plant.ratedPower = plant.product.maxPowerElectric;
			}
		}

		@Override
		public void createControl(Composite parent) {
			var root = new Composite(parent, SWT.NONE);
			setControl(root);
			UI.gridLayout(root, 1, 5, 5);
			var comp = UI.formComposite(root);
			UI.gridData(comp, true, false);
			nameField(comp);
			powerFilter(comp);
			boilerTable(root);
			updateBoilers();
		}

		private void nameField(Composite comp) {
			nameText = UI.formText(comp, M.Name);
			nameEdited = false;
			Texts.on(nameText).required().onChanged(t -> {
				Boiler b = Viewers.getFirstSelected(table);
				if (b == null || !Objects.equals(b.name, t)) {
					nameEdited = true;
				}
				validate();
			});
		}

		private void powerFilter(Composite comp) {
			powerFilter = UI.formText(comp, "Bemessungsleistung [kWel]");
			Texts.on(powerFilter).decimal().onChanged(s -> {
				updateBoilers();
				validate();
			});
		}

		private void boilerTable(Composite root) {
			table = Tables.createViewer(root,
					"Bezeichnung",
					"Hersteller",
					"Min. Leistung el.",
					"Max. Leistung el."
			);
			Tables.bindColumnWidths(table, 0.3, 0.3, 0.2, 0.2);
			table.setContentProvider(ArrayContentProvider.getInstance());
			table.setLabelProvider(new BoilerLabel());
			table.addSelectionChangedListener(e -> {
				suggestName();
				validate();
			});
		}

		private void updateBoilers() {
			var input = new ArrayList<Boiler>();
			double filter = Texts.getDouble(powerFilter);
			for (var b : App.getDb().getAll(Boiler.class)) {
				if (!Objects.equals(b.group, group) || !b.isCoGenPlant)
					continue;
				if (filter != 0
						&& (filter < b.minPowerElectric
						|| filter > (b.maxPowerElectric * 1.2)))
					continue;
				input.add(b);
			}
			Sorters.boilers(input);
			table.setInput(input);
		}

		private void suggestName() {
			if (nameEdited && !Texts.isEmpty(nameText))
				return;
			Boiler b = Viewers.getFirstSelected(table);
			if (b == null) {
				nameText.setText("");
			} else {
				Texts.set(nameText, b.name);
			}
		}

		private void validate() {
			if (Texts.isEmpty(nameText)) {
				error("Name darf nicht leer sein");
				return;
			}
			if (Viewers.getFirstSelected(table) == null) {
				error("Bitte wählen Sie ein Biogas-BHKW aus");
				return;
			}
			setErrorMessage(null);
			setPageComplete(true);
		}

		private void error(String message) {
			setErrorMessage(message);
			setPageComplete(false);
		}
	}

	private static class BoilerLabel extends LabelProvider
			implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof Boiler b))
				return null;
			return switch (col) {
				case 0 -> b.name;
				case 1 -> b.manufacturer != null
						? b.manufacturer.name
						: null;
				case 2 -> Num.str(b.minPowerElectric) + " kW";
				case 3 -> Num.str(b.maxPowerElectric) + " kW";
				default -> null;
			};
		}
	}
}
