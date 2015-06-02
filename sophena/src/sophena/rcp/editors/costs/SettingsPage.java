package sophena.rcp.editors.costs;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import sophena.rcp.utils.UI;

class SettingsPage extends FormPage {

	public SettingsPage(CostEditor editor) {
		super(editor, "BaseCostEditor.Page", "Kosteneinstellungen");
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Kosteneinstellungen");
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		Composite comp = UI.formSection(body, tk, "Kosteneinstellungen");
		UI.formText(comp, tk, "Kalkulatorischer Zinssatz (ohne Förderung):").setText("1,02");
		UI.formText(comp, tk, "Kalkulatorischer Zinssatz (mit Förderung):").setText("1,015");
		UI.formText(comp, tk, "Projektlaufzeit (Betrachtungszeitraum):").setText("20");
		UI.formText(comp, tk, "Preisänderungsfaktor (Investitionen):").setText("1,015");
		UI.formText(comp, tk, "Mehrwertsteuersatz:").setText("1,19");
		UI.formText(comp, tk, "Preisänderungsfaktor Biomasse-Brennstoff:").setText("1,02");
		UI.formText(comp, tk, "Preisänderungsfaktor fossiler Brennstoff:").setText("1,03");
		UI.formText(comp, tk, "Strompreis netto (€/MWh):").setText("250");
		UI.formText(comp, tk, "Preisänderungsfaktor Strom:").setText("1,03");
		UI.formText(comp, tk, "Preisänderungsfaktor Betriebsgebundene und sonstige Kosten:").setText("1,02");
		UI.formText(comp, tk, "Preisänderungsfaktor Instandhaltungskosten:").setText("1,02");
		UI.formText(comp, tk, "Stundenlohn:").setText("25");
		UI.formText(comp, tk, "Versicherung:").setText("0,0025");
		UI.formText(comp, tk, "Sonstige Abgaben (Steuern, Pacht, …):").setText("0,0025");
		UI.formText(comp, tk, "Verwaltung (muss eingegeben werden):").setText("0,005");
	}
}
