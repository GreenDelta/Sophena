package sophena.rcp.editors.biogas.substrate;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.model.biogas.Substrate;
import sophena.rcp.M;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

public class SubstrateWizard extends Wizard {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private Page page;
	private Substrate substrate;

	public static int open(Substrate substrate) {
		if (substrate == null)
			return Window.CANCEL;
		SubstrateWizard wiz = new SubstrateWizard();
		wiz.setWindowTitle("Biogas Substrat");
		wiz.substrate = substrate;
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		try {
			substrate.name = page.nameText.getText();
			substrate.description = page.descriptionText.getText();
			substrate.dryMatter = Texts.getDouble(page.dryMatterText);
			substrate.organicDryMatter = Texts.getDouble(page.organicDryMatterText);
			substrate.biogasProduction = Texts.getDouble(page.biogasProductionText);
			substrate.methaneContent = Texts.getDouble(page.methaneContentText);
			substrate.co2Emissions = Texts.getDouble(page.co2EmissionsText);
			return true;
		} catch (Exception e) {
			log.error("failed to set substrate data " + substrate, e);
			return false;
		}
	}

	@Override
	public void addPages() {
		page = new Page();
		page.setPageComplete(!substrate.isProtected);
		addPage(page);
	}

	private class Page extends WizardPage {

		private Text nameText;
		private Text descriptionText;
		private Text dryMatterText;
		private Text organicDryMatterText;
		private Text biogasProductionText;
		private Text methaneContentText;
		private Text co2EmissionsText;

		private Page() {
			super("SubstrateWizardPage", "Biogas Substrat", null);
			setMessage(" ");
		}

		@Override
		public void createControl(Composite parent) {
			Composite comp = new Composite(parent, SWT.NONE);
			setControl(comp);
			UI.gridLayout(comp, 3);
			createNameText(comp);
			createDryMatterText(comp);
			createOrganicDryMatterText(comp);
			createBiogasProductionText(comp);
			createMethaneContentText(comp);
			createCO2EmissionsText(comp);
			createDescriptionText(comp);
			validate();
		}

		private void createNameText(Composite comp) {
			nameText = UI.formText(comp, M.Name);
			Texts.on(nameText)
					.init(substrate.name)
					.required()
					.validate(this::validate);
			UI.formLabel(comp, "");
		}

		private void createDryMatterText(Composite comp) {
			dryMatterText = UI.formText(comp, "Trockensubstanz (TS)");
			Texts.on(dryMatterText)
					.init(substrate.dryMatter)
					.required()
					.decimal()
					.validate(this::validate);
			UI.formLabel(comp, "%");
		}

		private void createOrganicDryMatterText(Composite comp) {
			organicDryMatterText = UI.formText(comp, "Organische Trockensubstanz (oTS)");
			Texts.on(organicDryMatterText)
					.init(substrate.organicDryMatter)
					.required()
					.decimal()
					.validate(this::validate);
			UI.formLabel(comp, "%");
		}

		private void createBiogasProductionText(Composite comp) {
			biogasProductionText = UI.formText(comp, "Biogasproduktion");
			Texts.on(biogasProductionText)
					.init(substrate.biogasProduction)
					.required()
					.decimal()
					.validate(this::validate);
			UI.formLabel(comp, "m³/t oTS");
		}

		private void createMethaneContentText(Composite comp) {
			methaneContentText = UI.formText(comp, "Methangehalt");
			Texts.on(methaneContentText)
					.init(substrate.methaneContent)
					.required()
					.decimal()
					.validate(this::validate);
			UI.formLabel(comp, "%");
		}

		private void createCO2EmissionsText(Composite comp) {
			co2EmissionsText = UI.formText(comp, "CO2 Emissionen");
			Texts.on(co2EmissionsText)
					.init(substrate.co2Emissions)
					.decimal()
					.validate(this::validate);
			UI.formLabel(comp, "g CO2 äq./kWh");
		}

		private void createDescriptionText(Composite comp) {
			descriptionText = UI.formMultiText(comp, M.Description);
			Texts.set(descriptionText, substrate.description);
			UI.formLabel(comp, "");
		}

		private boolean validate() {
			if (Texts.isEmpty(nameText))
				return error("Es muss ein Name angegeben werden.");
			if (!Num.isNumeric(dryMatterText.getText()))
				return error("Es muss ein Wert für die Trockensubstanz angegeben werden.");
			if (!Num.isNumeric(organicDryMatterText.getText()))
				return error("Es muss ein Wert für die organische Trockensubstanz angegeben werden.");
			if (!Num.isNumeric(biogasProductionText.getText()))
				return error("Es muss ein Wert für die Biogasproduktion angegeben werden.");
			if (!Num.isNumeric(methaneContentText.getText()))
				return error("Es muss ein Wert für den Methangehalt angegeben werden.");
			setPageComplete(!substrate.isProtected);
			setErrorMessage(null);
			return true;
		}

		private boolean error(String message) {
			setErrorMessage(message);
			setPageComplete(false);
			return false;
		}
	}
}
