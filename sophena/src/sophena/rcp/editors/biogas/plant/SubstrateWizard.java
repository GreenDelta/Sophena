package sophena.rcp.editors.biogas.plant;

import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.RootEntityDao;
import sophena.model.Stats;
import sophena.model.biogas.Substrate;
import sophena.model.biogas.SubstrateProfile;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;
import sophena.utils.Strings;

class SubstrateWizard extends Wizard {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private Page page;
	private SubstrateProfile profile;

	public static int open(SubstrateProfile profile) {
		if (profile == null)
			return Window.CANCEL;
		SubstrateWizard wiz = new SubstrateWizard();
		wiz.setWindowTitle("Substratprofil bearbeiten");
		wiz.profile = profile;
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		try {
			page.data.bindToModel();
			return true;
		} catch (Exception e) {
			log.error("failed to set substrate profile data", e);
			return false;
		}
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private final DataBinding data = new DataBinding();

		private EntityCombo<Substrate> substrateCombo;
		private Text annualAmountText;
		private Text costsText;

		private Button monthlyRadio;
		private Button hourlyRadio;
		private Text[] monthlyTexts;
		private Text hourlyFileText;

		private Page() {
			super("SubstrateProfileWizardPage", "Substratprofil bearbeiten", null);
			setMessage("Definieren Sie ein Substratprofil für die Biogasanlage");
		}

		@Override
		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			setControl(composite);
			UI.gridLayout(composite, 1);

			createBasicSection(composite);
			createDistributionSection(composite);

			data.bindToUI();
		}

		private void createBasicSection(Composite parent) {
			Group group = new Group(parent, SWT.NONE);
			group.setText("Grunddaten");
			UI.gridData(group, true, false);
			UI.gridLayout(group, 3);

			// substrate combo
			substrateCombo = new EntityCombo<>();
			substrateCombo.create("Substrat", group);
			UI.formLabel(group, "");
			substrateCombo.onSelect((s) -> data.validate());

			// annual amount
			annualAmountText = UI.formText(group, "Jährliche Menge");
			Texts.on(annualAmountText).required().decimal();
			UI.formLabel(group, "t/a");
			annualAmountText.addModifyListener((e) -> data.validate());

			// costs
			costsText = UI.formText(group, "Substratkosten");
			Texts.on(costsText).required().decimal();
			UI.formLabel(group, "€/t");
			costsText.addModifyListener((e) -> data.validate());
		}

		private void createDistributionSection(Composite parent) {
			Group group = new Group(parent, SWT.NONE);
			group.setText("Verteilung");
			UI.gridData(group, true, true);
			UI.gridLayout(group, 1);

			// radio buttons
			Composite radioComp = new Composite(group, SWT.NONE);
			UI.gridData(radioComp, true, false);
			UI.gridLayout(radioComp, 2);

			monthlyRadio = new Button(radioComp, SWT.RADIO);
			monthlyRadio.setText("Monatliche Prozentwerte");
			monthlyRadio.setSelection(true);

			hourlyRadio = new Button(radioComp, SWT.RADIO);
			hourlyRadio.setText("Excel-Datei mit Stundenwerten");

			// monthly section
			createMonthlySection(group);

			// hourly section
			createHourlySection(group);

			// radio button listeners
			Controls.onSelect(monthlyRadio, (e) -> {
				setMonthlyEnabled(true);
				setHourlyEnabled(false);
			});

			Controls.onSelect(hourlyRadio, (e) -> {
				setMonthlyEnabled(false);
				setHourlyEnabled(true);
			});
		}

		private void createMonthlySection(Composite parent) {
			Group monthlyGroup = new Group(parent, SWT.NONE);
			monthlyGroup.setText("Monatliche Verteilung (%)");
			UI.gridData(monthlyGroup, true, false);
			UI.gridLayout(monthlyGroup, 4);

			monthlyTexts = new Text[12];
			String[] months = {"Jan", "Feb", "Mär", "Apr", "Mai", "Jun",
					"Jul", "Aug", "Sep", "Okt", "Nov", "Dez"};

			for (int i = 0; i < 12; i++) {
				monthlyTexts[i] = UI.formText(monthlyGroup, months[i]);
				Texts.on(monthlyTexts[i]).decimal();
				monthlyTexts[i].addModifyListener((e) -> data.validate());
			}
		}

		private void createHourlySection(Composite parent) {
			Group hourlyGroup = new Group(parent, SWT.NONE);
			hourlyGroup.setText("Excel-Datei");
			UI.gridData(hourlyGroup, true, false);
			UI.gridLayout(hourlyGroup, 3);

			hourlyFileText = UI.formText(hourlyGroup, "Datei");
			hourlyFileText.setEnabled(false);

			Button browseButton = new Button(hourlyGroup, SWT.PUSH);
			browseButton.setText("Durchsuchen...");
			browseButton.setEnabled(false);
			// TODO: Add file dialog when Excel reading is implemented

			setHourlyEnabled(false);
		}

		private void setMonthlyEnabled(boolean enabled) {
			if (monthlyTexts != null) {
				for (Text text : monthlyTexts) {
					text.setEnabled(enabled);
				}
			}
		}

		private void setHourlyEnabled(boolean enabled) {
			if (hourlyFileText != null) {
				hourlyFileText.setEnabled(enabled);
			}
		}

		private class DataBinding {

			void bindToModel() {
				profile.substrate = substrateCombo.getSelected();
				profile.annualAmount = Texts.getDouble(annualAmountText);
				profile.substrateCosts = Texts.getDouble(costsText);

				if (monthlyRadio.getSelection()) {
					// bind monthly values
					profile.monthlyPercentages = new double[12];
					for (int i = 0; i < 12; i++) {
						profile.monthlyPercentages[i] = Texts.getDouble(monthlyTexts[i]);
					}
					profile.hourlyValues = null;
				} else {
					// hourly values - for now just initialize empty array
					// TODO: Read from Excel file when implemented
					profile.hourlyValues = new double[Stats.HOURS];
					profile.monthlyPercentages = null;
				}
			}

			void bindToUI() {
				initSubstrates();

				Texts.set(annualAmountText, profile.annualAmount);
				Texts.set(costsText, profile.substrateCosts);

				// determine distribution type
				boolean hasMonthly = profile.monthlyPercentages != null &&
						profile.monthlyPercentages.length > 0;
				boolean hasHourly = profile.hourlyValues != null &&
						profile.hourlyValues.length > 0;

				if (hasHourly && !hasMonthly) {
					hourlyRadio.setSelection(true);
					monthlyRadio.setSelection(false);
					setMonthlyEnabled(false);
					setHourlyEnabled(true);
				} else {
					monthlyRadio.setSelection(true);
					hourlyRadio.setSelection(false);
					setMonthlyEnabled(true);
					setHourlyEnabled(false);

					// bind monthly values
					if (hasMonthly) {
						for (int i = 0; i < 12 && i < profile.monthlyPercentages.length; i++) {
							Texts.set(monthlyTexts[i], profile.monthlyPercentages[i]);
						}
					} else {
						// set default equal distribution
						double equalPercent = 100.0 / 12.0;
						for (int i = 0; i < 12; i++) {
							Texts.set(monthlyTexts[i], equalPercent);
						}
					}
				}

				validate();
			}

			private void initSubstrates() {
				var dao = new RootEntityDao<>(Substrate.class, App.getDb());
				List<Substrate> substrates = dao.getAll();
				substrates.sort(Sorters.byName());
				substrateCombo.setInput(substrates);

				if (substrates.isEmpty())
					return;

				Substrate s = profile.substrate != null ? profile.substrate : substrates.get(0);
				substrateCombo.select(s);
			}

			boolean validate() {
				Substrate substrate = substrateCombo.getSelected();
				if (substrate == null)
					return error("Bitte wählen Sie ein Substrat aus");

				if (Strings.nullOrEmpty(annualAmountText.getText()))
					return error("Bitte geben Sie die jährliche Menge an");

				if (Strings.nullOrEmpty(costsText.getText()))
					return error("Bitte geben Sie die Substratkosten an");

				if (monthlyRadio.getSelection()) {
					// validate monthly percentages
					double sum = 0;
					for (Text text : monthlyTexts) {
						if (!Strings.nullOrEmpty(text.getText())) {
							sum += Texts.getDouble(text);
						}
					}
					if (Math.abs(sum - 100.0) > 0.1) {
						return error("Die Summe der monatlichen Prozentwerte muss 100% betragen (aktuell: " +
								Num.str(sum) + "%)");
					}
				}

				setErrorMessage(null);
				setPageComplete(true);
				return true;
			}

			private boolean error(String message) {
				setErrorMessage(message);
				setPageComplete(false);
				return false;
			}
		}
	}
}
