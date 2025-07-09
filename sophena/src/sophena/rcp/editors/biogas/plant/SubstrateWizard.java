package sophena.rcp.editors.biogas.plant;

import java.io.File;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import sophena.model.HoursTrace;
import sophena.model.Stats;
import sophena.model.biogas.Substrate;
import sophena.model.biogas.SubstrateProfile;
import sophena.rcp.App;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Ref;

class SubstrateWizard extends Wizard {

	private Page page;
	private SubstrateProfile profile;

	public static int open(SubstrateProfile profile) {
		if (profile == null)
			return Window.CANCEL;
		var wiz = new SubstrateWizard();
		wiz.setWindowTitle("Substratprofil bearbeiten");
		wiz.profile = profile;
		var dialog = new WizardDialog(UI.shell(), wiz);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		var substrate = page.substrateCombo.getSelected();
		if (substrate == null) {
			MsgBox.error("Kein Substrat", "Es wurde kein ausgewählt");
			return false;
		}

		// from monthly distribution
		if (page.monthRadio.getSelection()) {
			var mass = page.monthPanel.mass();
			if (mass <= 0) {
				MsgBox.error("Keine Masse", "Es wurde keine gültige Masse angegeben");
				return false;
			}

			var percs = page.monthPanel.percentages();
			var sum = Stats.sum(percs);
			if (Math.abs(sum - 100) > 3) {
				MsgBox.error("Verteilung ergibt nicht 100%",
						"Die monatliche Verteilung ergibt nicht 100%");
				return false;
			}

			profile.substrate = substrate;
			profile.annualMass = mass;
			profile.monthlyPercentages = percs;
			profile.substrateCosts = Texts.getDouble(page.costsText);
			profile.hourlyValues = profileOf(mass, percs);
			return true;
		}

		// from Excel file
		var file = page.excelPanel.file();
		if (file == null) {
			MsgBox.error("Keine Datei",
					"Es wurde keine Excel-Datei mit Daten ausgewählt.");
			return false;
		}
		var data = SubstrateProfileIO.read(file).orElse(null);
		if (data == null)
			return false;

		var mass = Stats.sum(data);
		if (mass <= 0) {
			MsgBox.error("Keine Masse", "Der Lastgang summiert zu 0.");
			return false;
		}
		profile.substrate = substrate;
		profile.annualMass = mass;
		profile.monthlyPercentages = null;
		profile.substrateCosts = Texts.getDouble(page.costsText);
		profile.hourlyValues = data;
		return true;
	}

	private double[] profileOf(double mass, double[] percs) {
		var percSum = Stats.sum(percs);
		var days = HoursTrace.DAYS_IN_MONTH;
		int pos = 0;
		var profile = new double[Stats.HOURS];
		for (int month = 0; month < percs.length; month++) {
			var share = percs[month] / percSum;
			var hours = days[month] * 24;
			var massPerHour = mass * share / hours;
			for (var hour = 0; hour < hours; hour++) {
				profile[pos] = massPerHour;
				pos++;
			}
		}
		return profile;
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private EntityCombo<Substrate> substrateCombo;
		private Text costsText;
		private Button monthRadio;
		private Button excelRadio;

		private WizardMonthPanel monthPanel;
		private ExcelPanel excelPanel;

		private Page() {
			super("SubstrateProfileWizardPage", "Substratprofil bearbeiten", null);
		}

		@Override
		public void createControl(Composite parent) {
			var comp = new Composite(parent, SWT.NONE);
			setControl(comp);
			UI.gridLayout(comp, 1);
			createSubstratePanel(comp);
			monthPanel = WizardMonthPanel.create(comp, profile);
			excelPanel = ExcelPanel.create(comp);
			getShell().pack();

			var months = profile.monthlyPercentages != null;
			monthPanel.setEnabled(months);
			monthRadio.setSelection(months);
			excelPanel.setEnabled(!months);
			excelRadio.setSelection(!months);
		}

		private void createSubstratePanel(Composite parent) {
			var comp = new Group(parent, SWT.NONE);
			comp.setText("Substratauswahl");
			UI.gridData(comp, true, false);
			UI.gridLayout(comp, 3);

			// substrate
			substrateCombo = createSubstrateCombo(comp);

			// costs
			costsText = UI.formText(comp, "Substratkosten");
			Texts.on(costsText)
					.init(profile.substrateCosts)
					.required()
					.decimal();
			UI.formLabel(comp, "€/t");

			// radios
			UI.formLabel(comp, "Verteilung");
			var radioComp = new Composite(comp, SWT.NONE);
			UI.gridData(radioComp, true, false);
			UI.innerGrid(radioComp, 2);
			monthRadio = new Button(radioComp, SWT.RADIO);
			monthRadio.setText("Monatliche Prozentwerte");
			excelRadio = new Button(radioComp, SWT.RADIO);
			excelRadio.setText("Excel-Datei mit Stundenwerten");

			Controls.onSelect(monthRadio, (e) -> {
				monthPanel.setEnabled(true);
				excelPanel.setEnabled(false);
			});
			Controls.onSelect(excelRadio, (e) -> {
				monthPanel.setEnabled(false);
				excelPanel.setEnabled(true);
			});
		}

		private EntityCombo<Substrate> createSubstrateCombo(Composite comp) {
			var combo = new EntityCombo<Substrate>();
			combo.create("Substrat", comp);
			UI.filler(comp);
			var substrates = App.getDb().getAll(Substrate.class);
			if (substrates.isEmpty())
				return combo;
			substrates.sort(Sorters.byName());
			combo.setInput(substrates);
			Substrate s = profile.substrate != null
					? profile.substrate
					: substrates.getFirst();
			combo.select(s);
			return combo;
		}

		private record ExcelPanel(Text text, Button button, Ref<File> fileRef) {

			static ExcelPanel create(Composite comp) {
				var group = new Group(comp, SWT.NONE);
				group.setText("Excel-Datei");
				UI.gridData(group, true, false);
				UI.gridLayout(group, 3);

				var text = UI.formText(group, "Datei");
				var button = new Button(group, SWT.PUSH);
				button.setText("Durchsuchen...");

				var ref = new Ref<File>();
				Controls.onSelect(button, $ -> {
					var file = FileChooser.open(".xlsx");
					if (file == null)
						return;
					text.setText(file.getName());
					ref.set(file);
				});

				return new ExcelPanel(text, button, ref);
			}

			void setEnabled(boolean b) {
				text.setEnabled(b);
				button.setEnabled(b);
			}

			File file() {
				return fileRef != null ? fileRef.get() : null;
			}
		}
	}
}
