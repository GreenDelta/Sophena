package sophena.rcp.editors.consumers;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.model.Consumer;
import sophena.rcp.M;
import sophena.rcp.editors.LoadCurveSection;
import sophena.rcp.utils.UI;

class InfoPage extends FormPage {

	private ConsumerEditor editor;

	public InfoPage(ConsumerEditor editor) {
		super(editor, "sophena.ConsumerInfoPage", M.ConsumerInformation);
		this.editor = editor;
	}

	private Consumer consumer() {
		return editor.consumer;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, consumer().name);
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		InfoSection infoSection = InfoSection.of(editor).create(body, tk);
		infoSection.setDemandSection(
				HeatDemandSection.of(editor).create(body, tk));
		if (!consumer().demandBased)
			ConsumptionSection.of(editor).create(body, tk);
		InterruptionSection.of(editor).create(body, tk);
		LoadCurveSection loadCurve = new LoadCurveSection();
		loadCurve.render(body, tk);
		new TransferStationSection(editor).create(body, tk);
		editor.onCalculated((profile, totals, total) -> loadCurve.setData(profile));
		form.reflow(true);
		editor.calculate();
	}
}
