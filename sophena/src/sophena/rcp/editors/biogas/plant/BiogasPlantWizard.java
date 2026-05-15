package sophena.rcp.editors.biogas.plant;

import java.util.Optional;
import java.util.UUID;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sophena.model.FuelGroup;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.BiogasPlantBoiler;
import sophena.rcp.M;
import sophena.rcp.app.App;
import sophena.rcp.app.Icon;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.MsgBox;
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
		page = new Page(plant);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		page.update(plant);
		App.getDb().insert(plant);
		return true;
	}

	private static class Page extends WizardPage {

		private final BiogasPlant plant;
		private Text nameText;
		private boolean nameEdited;
		private TableViewer table;
		private Text ratedPowerText;

		private Page(BiogasPlant plant) {
			super("BiogasPlantWizardPage", "Neue Biogasanlage", null);
			setMessage(" ");
			this.plant = plant;
			setPageComplete(false);
		}

		private void update(BiogasPlant plant) {
			plant.name = nameText.getText();
			plant.ratedPower = Texts.getDouble(ratedPowerText);
			if (plant.ratedPower == 0) {
				plant.ratedPower = plant.totalElectricPower();
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
			ratedPowerField(comp);
			boilerToolbar(root);
			boilerTable(root);
		}

		private void nameField(Composite comp) {
			nameText = UI.formText(comp, M.Name);
			nameEdited = false;
			Texts.on(nameText).required().onChanged(t -> {
				var first = firstBoiler();
				if (first == null || !java.util.Objects.equals(first.name, t)) {
					nameEdited = true;
				}
				validate();
			});
		}

		private void ratedPowerField(Composite comp) {
			ratedPowerText = UI.formText(comp, "Bemessungsleistung [kWel]");
			Texts.on(ratedPowerText).decimal().onChanged(s -> validate());
		}

		private void boilerToolbar(Composite root) {
			var comp = new Composite(root, SWT.NONE);
			UI.gridLayout(comp, 2);
			UI.gridData(comp, true, false);
			UI.formLabel(comp, "BHKW-Blöcke");
			Action add = Actions.create(M.Add, Icon.ADD_16.des(), this::addBoiler);
			Action edit = Actions.create(M.Edit, Icon.EDIT_16.des(), this::editBoiler);
			Action remove = Actions.create(M.Remove, Icon.DELETE_16.des(), this::removeBoiler);
			Actions.bind(comp, add, edit, remove);
		}

		private void boilerTable(Composite root) {
			table = Tables.createViewer(root,
					"Bezeichnung",
					"Hersteller",
					"Max. Wärmeleistung",
					"Max. Leistung el.",
					"Investition"
			);
			Tables.bindColumnWidths(table, 0.30, 0.20, 0.18, 0.18, 0.14);
			table.setLabelProvider(new BoilerLabel());
			table.addSelectionChangedListener(e -> {
				suggestName();
				validate();
			});
			Tables.onDoubleClick(table, e -> editBoiler());
			table.setInput(plant.boilers);
		}

		private void addBoiler() {
			var entry = new BiogasPlantBoiler();
			entry.id = UUID.randomUUID().toString();
			if (BiogasPlantBoilerWizard.open(entry, plant.productGroup) != Window.OK)
				return;
			plant.boilers.add(entry);
			if (Texts.getDouble(ratedPowerText) == 0) {
				Texts.set(ratedPowerText, plant.totalElectricPower());
			}
			table.setInput(plant.boilers);
			suggestName();
			validate();
		}

		private void editBoiler() {
			BiogasPlantBoiler entry = Viewers.getFirstSelected(table);
			entry = sophena.utils.Lists.find(entry, plant.boilers);
			if (entry == null)
				return;
			var clone = entry.copy();
			if (BiogasPlantBoilerWizard.open(clone, plant.productGroup) != Window.OK)
				return;
			entry.boiler = clone.boiler;
			entry.costs = clone.costs;
			table.setInput(plant.boilers);
			suggestName();
			validate();
		}

		private void removeBoiler() {
			var entries = sophena.utils.Lists.findAll(
					Viewers.getAllSelected(table), plant.boilers);
			if (entries.isEmpty())
				return;
			plant.boilers.removeAll(entries);
			table.setInput(plant.boilers);
			validate();
		}

		private sophena.model.Boiler firstBoiler() {
			if (plant.boilers.isEmpty())
				return null;
			var first = plant.boilers.get(0);
			return first != null ? first.boiler : null;
		}

		private void suggestName() {
			if (nameEdited && !Texts.isEmpty(nameText))
				return;
			var boiler = firstBoiler();
			if (boiler == null) {
				nameText.setText("");
			} else if (plant.boilers.size() == 1) {
				Texts.set(nameText, boiler.name);
			} else {
				Texts.set(nameText, "Biogasanlage");
			}
		}

		private void validate() {
			if (Texts.isEmpty(nameText)) {
				error("Name darf nicht leer sein");
				return;
			}
			if (plant.boilers.isEmpty()) {
				error("Bitte fügen Sie mindestens einen BHKW-Block hinzu");
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

	private static class BoilerLabel extends org.eclipse.jface.viewers.LabelProvider
			implements org.eclipse.jface.viewers.ITableLabelProvider {

		@Override
		public org.eclipse.swt.graphics.Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof BiogasPlantBoiler entry) || entry.boiler == null)
				return null;
			return switch (col) {
				case 0 -> entry.boiler.name;
				case 1 -> entry.boiler.manufacturer != null
						? entry.boiler.manufacturer.name
						: null;
				case 2 -> Num.str(entry.boiler.maxPower) + " kW";
				case 3 -> Num.str(entry.boiler.maxPowerElectric) + " kW";
				case 4 -> entry.costs != null
						? Num.str(entry.costs.investment) + " EUR"
						: null;
				default -> null;
			};
		}
	}
}
