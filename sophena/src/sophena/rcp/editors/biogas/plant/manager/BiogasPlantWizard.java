package sophena.rcp.editors.biogas.plant.manager;

import java.util.Optional;
import java.util.UUID;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sophena.model.FuelGroup;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.model.biogas.BiogasPlant;
import sophena.rcp.M;
import sophena.rcp.app.App;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

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
		private Text durationText;
		private Text descriptionText;

		private Page(BiogasPlant plant) {
			super("BiogasPlantWizardPage", "Neue Biogasanlage", null);
			setMessage(" ");
			this.plant = plant;
			setPageComplete(false);
		}

		private void update(BiogasPlant plant) {
			plant.name = nameText.getText();
			plant.duration = Texts.getInt(durationText);
			plant.description = descriptionText.getText();
		}

		@Override
		public void createControl(Composite parent) {
			var comp = UI.formComposite(parent);
			setControl(comp);
			UI.gridLayout(comp, 3);
			nameField(comp);
			durationField(comp);
			descriptionField(comp);
		}

		private void nameField(Composite comp) {
			nameText = UI.formText(comp, M.Name);
			Texts.on(nameText).required().validate(this::validate);
			UI.filler(comp);
		}

		private void durationField(Composite comp) {
			durationText = UI.formText(comp, "Laufzeit (Jahre)");
			Texts.on(durationText).required().integer().validate(this::validate);
			UI.filler(comp);
		}

		private void descriptionField(Composite comp) {
			descriptionText = UI.formMultiText(comp, M.Description);
			UI.filler(comp);
		}

		private void validate() {
			if (Texts.isEmpty(nameText)) {
				error("Name darf nicht leer sein");
				return;
			}
			if (!Texts.hasNumber(durationText) || Texts.getInt(durationText) <= 0) {
				error("Bitte geben Sie eine gültige Laufzeit an");
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
}
