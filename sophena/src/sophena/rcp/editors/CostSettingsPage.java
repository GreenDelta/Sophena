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
import sophena.rcp.help.H;
import sophena.rcp.help.HelpLink;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.Texts.TextDispatch;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

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
		String title = forProject ? "Projekt-" : "Basis-";
		title += "Kosteneinstellungen";
		ScrolledForm form  = UI.formHeader(mform, title);
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
		UI.gridLayout(c, 4);
		t(c, "Mehrwertsteuersatz", "%", costs.vatRate)
				.onChanged(s -> costs.vatRate = Num.read(s));
		UI.filler(c);
		t(c, "Mittlerer Stundenlohn", "EUR", costs.hourlyWage)
				.onChanged(s -> costs.hourlyWage = Num.read(s));
		UI.filler(c);
		t(c, "Strompreis", "EUR/kWh", costs.electricityPrice)
				.onChanged(s -> costs.electricityPrice = Num.read(s));
		UI.filler(c);
		t(c, "Eigenstrombedarf", "%", costs.electricityDemandShare)
				.onChanged(s -> costs.electricityDemandShare = Num.read(s));
		HelpLink.create(c, toolkit, "Eigenstrombedarf", H.ElectricityDemandShare);
		if (forProject) {
			t(c, "Mittlere Stromerlöse", "EUR/kWh", Num.str(costs.electricityRevenues, 4))
					.onChanged(s -> costs.electricityRevenues = Num.read(s));
			HelpLink.create(c, toolkit, "Mittlere Stromerlöse", H.ElectricityRevenues);
		}
	}

	private void financeSection(Composite body) {
		Composite c = UI.formSection(body, toolkit, "Finanzierung");
		UI.gridLayout(c, 3);
		t(c, "Kapital-Mischzinssatz (ohne Förderung)", "%", costs.interestRate)
				.onChanged(s -> costs.interestRate = Num.read(s));

		t(c, "Kapital-Mischzinssatz (mit Förderung)", "%",
				costs.interestRateFunding)
						.onChanged(s -> costs.interestRateFunding = Num
								.read(s));
		if (forProject) {
			t(c, "Investitionsförderung", "EUR", costs.funding)
					.onChanged(s -> costs.funding = Num.read(s));
			t(c, "Einmalige Anschlusskosten", "EUR", 0);
		}
	}

	private void createOtherSection(Composite body) {
		Composite c = UI.formSection(body, toolkit, "Sonstige Kosten");
		UI.gridLayout(c, 3);
		t(c, "Versicherung", "%", costs.insuranceShare)
				.onChanged(s -> costs.insuranceShare = Num.read(s));
		t(c, "Sonstige Abgaben (Steuern, Pacht, …)", "%", costs.otherShare)
				.onChanged(s -> costs.otherShare = Num.read(s));
		t(c, "Verwaltung", "%", costs.administrationShare)
				.onChanged(s -> costs.administrationShare = Num.read(s));
	}

	private void createPriceChangeSection(Composite body) {
		Composite c = UI.formSection(body, toolkit, "Preisänderungsfaktoren");
		UI.gridLayout(c, 3);
		t(c, "Investitionen", "", costs.investmentFactor)
				.onChanged(s -> costs.investmentFactor = Num.read(s));
		t(c, "Biomasse-Brennstoff", "", costs.bioFuelFactor)
				.onChanged(s -> costs.bioFuelFactor = Num.read(s));
		t(c, "Fossiler Brennstoff", "", costs.fossilFuelFactor)
				.onChanged(s -> costs.fossilFuelFactor = Num.read(s));
		t(c, "Strom", "", costs.electricityFactor)
				.onChanged(s -> costs.electricityFactor = Num.read(s));
		t(c, "Betriebsgebundene und sonstige Kosten", "", costs.operationFactor)
				.onChanged(s -> costs.operationFactor = Num.read(s));
		t(c, "Instandhaltung", "", costs.maintenanceFactor)
				.onChanged(s -> costs.maintenanceFactor = Num.read(s));

	}

	private TextDispatch t(Composite comp, String label, String unit, double initial) {
		return t(comp, label, unit, Num.str(initial));
	}

	private TextDispatch t(Composite comp, String label, String unit, String initial) {
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
