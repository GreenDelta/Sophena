package sophena.rcp.editors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
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
		if (costs != null) {
			generalSection(body);
			financeSection(body);
			createOtherSection(body);
			createPriceChangeSection(body);
		}
		form.reflow(true);
	}

	private void generalSection(Composite body) {
		Composite c = UI.formSection(body, toolkit, "Allgemein");
		UI.gridLayout(c, 3);
		t(c, "Mehrwertsteuersatz", "%", costs.vatRate)
				.onChanged(s -> costs.vatRate = Numbers.read(s));
		t(c, "Mittlerer Stundenlohn", "EUR", costs.hourlyWage)
				.onChanged(s -> costs.hourlyWage = Numbers.read(s));
		t(c, "Strompreis (netto)", "EUR/kWh", costs.electricityPrice)
				.onChanged(s -> costs.electricityPrice = Numbers.read(s));
		t(c, "Eigenstrombedarf", "%", costs.electricityConsumption)
				.onChanged(s -> costs.electricityConsumption = Numbers.read(s));
		if (forProject) {
			t(c, "Mittlere Stromerlöse", "EUR/Jahr",
					costs.electricityRevenues)
							.onChanged(
									s -> costs.electricityRevenues = Numbers.read(s));
		}
	}

	private void financeSection(Composite body) {
		Composite c = UI.formSection(body, toolkit, "Finanzierung");
		UI.gridLayout(c, 3);
		t(c, "Kapital-Mischzinssatz (ohne Förderung)", "%", costs.interestRate)
				.onChanged(s -> costs.interestRate = Numbers.read(s));

		t(c, "Kapital-Mischzinssatz (mit Förderung)", "%",
				costs.interestRateFunding)
						.onChanged(s -> costs.interestRateFunding = Numbers
								.read(s));
		if (forProject) {
			t(c, "Investitionsförderung", "EUR", costs.funding)
					.onChanged(s -> costs.funding = Numbers.read(s));
			t(c, "Einmalige Anschlussgebühren", "EUR", 0);
		}
	}

	private void createOtherSection(Composite body) {
		Composite c = UI.formSection(body, toolkit, "Sonstige Kosten");
		UI.gridLayout(c, 3);
		t(c, "Versicherung", "%", costs.insuranceShare)
				.onChanged(s -> costs.insuranceShare = Numbers.read(s));

		t(c, "Sonstige Abgaben (Steuern, Pacht, …)", "%", costs.otherShare)
				.onChanged(s -> costs.otherShare = Numbers.read(s));

		t(c, "Verwaltung", "%", costs.administrationShare)
				.onChanged(s -> costs.administrationShare = Numbers.read(s));
	}

	private void createPriceChangeSection(Composite body) {
		Composite c = UI.formSection(body, toolkit, "Preisänderungsfaktoren");
		UI.gridLayout(c, 3);
		t(c, "Investitionen", "", costs.investmentFactor)
				.onChanged(s -> costs.investmentFactor = Numbers.read(s));
		t(c, "Biomasse-Brennstoff", "", costs.bioFuelFactor)
				.onChanged(s -> costs.bioFuelFactor = Numbers.read(s));
		t(c, "Fossiler Brennstoff", "", costs.fossilFuelFactor)
				.onChanged(s -> costs.fossilFuelFactor = Numbers.read(s));
		t(c, "Strom", "", costs.electricityFactor)
				.onChanged(s -> costs.electricityFactor = Numbers.read(s));
		t(c, "Betriebsgebundene und sonstige Kosten", "", costs.operationFactor)
				.onChanged(s -> costs.operationFactor = Numbers.read(s));
		t(c, "Instandhaltung", "", costs.maintenanceFactor)
				.onChanged(s -> costs.maintenanceFactor = Numbers
						.read(s));

	}

	private TextDispatch t(Composite comp, String label, String unit,
			double initial) {
		Label lab = UI.formLabel(comp, toolkit, label);
		UI.gridData(lab, false, false).widthHint = 250;
		Text text = toolkit.createText(comp, null, SWT.BORDER);
		UI.gridData(text, false, false).widthHint = 250;
		UI.formLabel(comp, toolkit, unit);
		return Texts.on(text)
				.required()
				.decimal()
				.init(initial)
				.onChanged(s -> editor.setDirty());
	}
}
