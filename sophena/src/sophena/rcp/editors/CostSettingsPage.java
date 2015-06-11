package sophena.rcp.editors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import sophena.model.CostSettings;
import sophena.rcp.utils.DataBinding;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

public class CostSettingsPage extends FormPage {

	private CostSettings costs;
	private DataBinding bind;
	private Composite composite;
	private FormToolkit toolkit;

	public CostSettingsPage(Editor editor, CostSettings costs) {
		super(editor, "CostSettingsPage", "Kosteneinstellungen");
		this.bind = new DataBinding(editor);
		this.costs = costs;
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
		bind.onDouble(
				t("Kalkulatorischer Zinssatz (ohne Förderung)", ""),
				() -> costs::getInterestRate,
				() -> costs::setInterestRate);
		bind.onDouble(
				t("Kalkulatorischer Zinssatz (mit Förderung)", ""),
				() -> costs::getInterestRateFunding,
				() -> costs::setInterestRateFunding);
		bind.onDouble(
				t("Preisänderungsfaktor (Investitionen)", ""),
				() -> costs::getInvestmentFactor,
				() -> costs::setInvestmentFactor);
		bind.onDouble(
				t("Mehrwertsteuersatz", ""),
				() -> costs::getVatRate,
				() -> costs::setVatRate);
		bind.onDouble(
				t("Preisänderungsfaktor Biomasse-Brennstoff", ""),
				() -> costs::getBioFuelFactor,
				() -> costs::setBioFuelFactor);
		bind.onDouble(
				t("Preisänderungsfaktor fossiler Brennstoff", ""),
				() -> costs::getFossilFuelFactor,
				() -> costs::setFossilFuelFactor);
		bind.onDouble(
				t("Strompreis netto", "EUR/MWh"),
				() -> costs::getElectricityPrice,
				() -> costs::setElectricityPrice);
		bind.onDouble(
				t("Preisänderungsfaktor Strom", ""),
				() -> costs::getElectricityFactor,
				() -> costs::setElectricityFactor);
		bind.onDouble(
				t("Preisänderungsfaktor betriebsgebundene und sonstige Kosten", ""),
				() -> costs::getOperationFactor,
				() -> costs::setOperationFactor);
		bind.onDouble(
				t("Preisänderungsfaktor Instandhaltungskosten", ""),
				() -> costs::getMaintenanceFactor,
				() -> costs::setMaintenanceFactor);
		bind.onDouble(
				t("Stundenlohn", "EUR"),
				() -> costs::getHourlyWage,
				() -> costs::setHourlyWage);
		bind.onDouble(
				t("Versicherung", "%"),
				() -> costs::getInsuranceShare,
				() -> costs::setInsuranceShare);
		bind.onDouble(
				t("Sonstige Abgaben (Steuern, Pacht, …)", "%"),
				() -> costs::getOtherShare,
				() -> costs::setOtherShare);
		bind.onDouble(
				t("Verwaltung", "%"),
				() -> costs::getAdministrationShare,
				() -> costs::setAdministrationShare);
	}

	private Text t(String label, String unit) {
		Text text = UI.formText(composite, toolkit, label);
		UI.gridData(text, false, false).widthHint = 250;
		Texts.on(text).required().decimal();
		UI.formLabel(composite, toolkit, unit);
		return text;
	}
}
