package sophena.rcp.editors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.model.CostSettings;
import sophena.rcp.Numbers;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.Texts.TextDispatch;
import sophena.rcp.utils.UI;

public class CostSettingsPage extends FormPage {

	private Editor editor;
	private CostSettings costs;
	private Composite composite;
	private FormToolkit toolkit;
	private boolean forProject;

	public CostSettingsPage(Editor editor, CostSettings costs) {
		super(editor, "CostSettingsPage", "Kosteneinstellungen");
		this.editor = editor;
		this.costs = costs;
	}

	public void setForProject(boolean forProject) {
		this.forProject = forProject;
	}

	public CostSettings getCosts() {
		return costs;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Kosteneinstellungen");
		toolkit = mform.getToolkit();
		Composite body = UI.formBody(form, toolkit);
		composite = UI.formSection(body, toolkit, "Kosteneinstellungen");
		UI.gridLayout(composite, 3);
		if (costs != null)
			createFields();
	}

	private void createFields() {

		t("Kalkulatorischer Zinssatz (ohne Förderung)", "", costs.interestRate)
				.onChanged((s) -> costs.interestRate = Numbers.read(s));

		t("Kalkulatorischer Zinssatz (mit Förderung)", "",
				costs.interestRateFunding)
				.onChanged((s) -> costs.interestRateFunding = Numbers.read(s));

		t("Preisänderungsfaktor (Investitionen)", "", costs.investmentFactor)
				.onChanged((s) -> costs.investmentFactor = Numbers.read(s));

		if(forProject) {
			t("Investitionsförderung", "EUR", costs.funding)
					.onChanged((s) -> costs.funding = Numbers.read(s));
			t("Erwartete jährliche Stromerlöse", "EUR/a",
					costs.electricityRevenues)
					.onChanged((s) -> costs.electricityRevenues = Numbers.read(s));
		}

		t("Mehrwertsteuersatz", "", costs.vatRate)
				.onChanged((s) -> costs.vatRate = Numbers.read(s));

		t("Preisänderungsfaktor Biomasse-Brennstoff", "", costs.bioFuelFactor)
				.onChanged((s) -> costs.bioFuelFactor = Numbers.read(s));

		t("Preisänderungsfaktor fossiler Brennstoff", "",
				costs.fossilFuelFactor)
				.onChanged((s) -> costs.fossilFuelFactor = Numbers.read(s));

		t("Strompreis netto", "EUR/MWh", costs.electricityPrice)
				.onChanged((s) -> costs.electricityPrice = Numbers.read(s));

		t("Preisänderungsfaktor Strom", "", costs.electricityFactor)
				.onChanged((s) -> costs.electricityFactor = Numbers.read(s));

		t("Preisänderungsfaktor betriebsgebundene und sonstige Kosten",
				"", costs.operationFactor)
				.onChanged((s) -> costs.operationFactor = Numbers.read(s));

		t("Preisänderungsfaktor Instandhaltungskosten", "",
				costs.maintenanceFactor)
				.onChanged((s) -> costs.maintenanceFactor = Numbers.read(s));

		t("Stundenlohn", "EUR", costs.hourlyWage)
				.onChanged((s) -> costs.hourlyWage = Numbers.read(s));

		t("Versicherung", "%", costs.insuranceShare)
				.onChanged((s) -> costs.insuranceShare = Numbers.read(s));

		t("Sonstige Abgaben (Steuern, Pacht, …)", "%", costs.otherShare)
				.onChanged((s) -> costs.otherShare = Numbers.read(s));

		t("Verwaltung", "%", costs.administrationShare)
				.onChanged((s) -> costs.administrationShare = Numbers.read(s));
	}

	private TextDispatch t(String label, String unit, double initial) {
		Text text = UI.formText(composite, toolkit, label);
		UI.gridData(text, false, false).widthHint = 250;
		UI.formLabel(composite, toolkit, unit);
		return Texts.on(text)
				.required()
				.decimal()
				.init(initial)
				.onChanged((s) -> editor.setDirty());
	}
}
