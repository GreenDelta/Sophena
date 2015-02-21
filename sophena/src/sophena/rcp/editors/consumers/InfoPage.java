package sophena.rcp.editors.consumers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.model.Consumer;
import sophena.rcp.utils.UI;

class InfoPage extends FormPage {

	private ConsumerEditor editor;

	public InfoPage(ConsumerEditor editor) {
		super(editor, "sophena.ConsumerInfoPage", "#Abnehmerinformationen");
		this.editor = editor;
	}

	private Consumer consumer() {
		return editor.getConsumer();
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		ScrolledForm form = UI.formHeader(managedForm, "#Project name");
		FormToolkit toolkit = managedForm.getToolkit();
		Composite body = UI.formBody(form, toolkit);
		createInfoSection(body, toolkit);
		createAddressSection(body, toolkit);
		createConsumptionSection(body, toolkit);
		form.reflow(true);
	}

	private void createInfoSection(Composite body, FormToolkit toolkit) {
		Composite composite = UI.formSection(body, toolkit, "#Abnehmer");
		Text nt = UI.formText(composite, toolkit, "#Name");
		nt.addModifyListener((e) -> consumer().setName(nt.getText()));
		Text dt = UI.formMultiText(composite, toolkit, "#Beschreibung");
		dt.addModifyListener((e) -> consumer().setDescription(dt.getText()));
		UI.formCombo(composite, toolkit, "#Gebäudetype");
		UI.formCombo(composite, toolkit, "#Gebäudezustand");
	}

	private void createAddressSection(Composite body, FormToolkit toolkit) {
		Composite composite = UI.formSection(body, toolkit, "#Adresse");
		UI.formText(composite, toolkit, "#Straße");
		UI.formText(composite, toolkit, "#Nummer");
		UI.formText(composite, toolkit, "#PLZ");
		UI.formText(composite, toolkit, "#Ort");
		UI.formText(composite, toolkit, "#GIS-Koordinaten");
	}

	private void createConsumptionSection(Composite body, FormToolkit tk) {
		Composite comp = UI.formSection(body, tk, "#Verbrach");
		tk.createLabel(comp, null);
		tk.createButton(comp, "#Verbrauchsgebundene Ermittlung", SWT.RADIO);
		tk.createLabel(comp, null);
		tk.createButton(comp, "#Bedarfsgebundene Ermittlung", SWT.RADIO);
		tk.createLabel(comp, "#Heizlast");
		Composite childComp = tk.createComposite(comp);
		UI.gridLayout(childComp, 2, 10, 0);
		Text heatLoad = tk.createText(childComp, null);
		UI.gridData(heatLoad, true, false);
		tk.createLabel(childComp, "#kW");
		tk.createLabel(comp, null);
		tk.createButton(comp, "#Verbrauchsdaten erfassen", SWT.NONE);
	}

}
