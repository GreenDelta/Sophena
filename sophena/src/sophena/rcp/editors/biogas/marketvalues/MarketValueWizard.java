package sophena.rcp.editors.biogas.marketvalues;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.model.biogas.ElectricityMarketValue;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

public class MarketValueWizard extends Wizard {

	private static final String[] MONTHS = {
		"Januar", "Februar", "März", "April", "Mai", "Juni",
		"Juli", "August", "September", "Oktober", "November", "Dezember"
	};

	private final Logger log = LoggerFactory.getLogger(getClass());
	private Page page;
	private ElectricityMarketValue value;

	public static int open(ElectricityMarketValue value) {
		if (value == null)
			return Window.CANCEL;
		var wiz = new MarketValueWizard();
		wiz.setWindowTitle("Marktwerte");
		wiz.value = value;
		var dialog = new WizardDialog(UI.shell(), wiz);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		try {
			value.name = page.nameText.getText();
			value.year = Texts.getInt(page.yearText);
			value.value = Texts.getDouble(page.valueText);
			value.source = page.sourceText.getText();
			var months = new double[MONTHS.length];
			for (int i = 0; i < months.length; i++) {
				months[i] = Texts.getDouble(page.monthTexts[i]);
			}
			value.monthlyValues = months;
			return true;
		} catch (Exception e) {
			log.error("failed to set market value data {}", value, e);
			return false;
		}
	}

	@Override
	public void addPages() {
		page = new Page();
		page.setPageComplete(!value.isProtected);
		addPage(page);
	}

	private class Page extends WizardPage {

		private Text nameText;
		private Text yearText;
		private Text valueText;
		private Text sourceText;
		private final Text[] monthTexts = new Text[MONTHS.length];

		private Page() {
			super("MarketValueWizardPage", "Marktwerte", null);
			setMessage(" ");
		}

		@Override
		public void createControl(Composite parent) {
			var comp = new Composite(parent, SWT.NONE);
			setControl(comp);
			UI.gridLayout(comp, 4);
			createNameText(comp);
			createYearText(comp);
			createValueText(comp);
			createSourceText(comp);
			createMonthTexts(comp);
			validate();
		}

		private void createNameText(Composite comp) {
			UI.formLabel(comp, "Name des Datensatzes");
			nameText = UI.formText(comp, (String) null);
			span(nameText, 3);
			Texts.on(nameText)
				.init(value.name)
				.required()
				.validate(this::validate);
		}

		private void createYearText(Composite comp) {
			UI.formLabel(comp, "Bezugsjahr");
			yearText = UI.formText(comp, (String) null);
			Texts.on(yearText)
				.init(value.year)
				.required()
				.integer()
				.validate(this::validate);
			UI.filler(comp);
			UI.filler(comp);
		}

		private void createValueText(Composite comp) {
			UI.formLabel(comp, "Jahresdurchschnitt");
			valueText = UI.formText(comp, (String) null);
			Texts.on(valueText)
				.init(value.value)
				.required()
				.decimal()
				.validate(this::validate);
			UI.formLabel(comp, "ct/kWh");
			UI.filler(comp);
		}

		private void createSourceText(Composite comp) {
			UI.formLabel(comp, "Datenquelle");
			sourceText = UI.formText(comp, (String) null);
			span(sourceText, 3);
			Texts.on(sourceText).init(value.source);
		}

		private void createMonthTexts(Composite comp) {
			var header = UI.formLabel(comp, "Monatswerte [ct/kWh]");
			span(header, 4);
			int half = MONTHS.length / 2;
			for (int i = 0; i < half; i++) {
				UI.formLabel(comp, MONTHS[i]);
				monthTexts[i] = monthText(comp, i);
				UI.formLabel(comp, MONTHS[i + half]);
				monthTexts[i + half] = monthText(comp, i + half);
			}
		}

		private Text monthText(Composite comp, int idx) {
			var text = UI.formText(comp, (String) null);
			Texts.on(text)
				.init(value.monthlyValueOf(idx + 1))
				.required()
				.decimal()
				.validate(this::validate);
			return text;
		}

		private void span(Control control, int columns) {
			if (control != null && control.getLayoutData() instanceof GridData gd) {
				gd.horizontalSpan = columns;
			}
		}

		private boolean validate() {
			if (Texts.isEmpty(nameText))
				return error("Es muss ein Name angegeben werden.");
			if (!Num.isNumeric(yearText.getText()))
				return error("Es muss ein Bezugsjahr angegeben werden.");
			if (Texts.getInt(yearText) <= 0)
				return error("Das Bezugsjahr muss größer als 0 sein.");
			if (!Num.isNumeric(valueText.getText()))
				return error("Es muss ein Jahresdurchschnitt angegeben werden.");
			for (int i = 0; i < monthTexts.length; i++) {
				if (monthTexts[i] == null
					|| !Num.isNumeric(monthTexts[i].getText()))
					return error("Es muss ein Wert für " + MONTHS[i]
						+ " angegeben werden.");
			}
			setPageComplete(!value.isProtected);
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
