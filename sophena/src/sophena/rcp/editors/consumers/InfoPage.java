package sophena.rcp.editors.consumers;

import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;

import sophena.model.Consumer;
import sophena.rcp.M;
import sophena.rcp.editors.LoadCurveSection;
import sophena.rcp.utils.UI;

class InfoPage extends FormPage {

	private final ConsumerEditor editor;

	public InfoPage(ConsumerEditor editor) {
		super(editor, "sophena.ConsumerInfoPage", M.ConsumerInformation);
		this.editor = editor;
	}

	private Consumer consumer() {
		return editor.consumer;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		var form = UI.formHeader(mform, consumer().name);
		var tk = mform.getToolkit();
		var body = UI.formBody(form, tk);
		var infoSection = InfoSection.of(editor).create(body, tk);
		infoSection.setDemandSection(
				HeatDemandSection.of(editor).create(body, tk));
		if (!consumer().hasProfile()) {
			if (!consumer().demandBased) {
				ConsumptionSection.of(editor).create(body, tk);
			}
			InterruptionSection.of(editor).create(body, tk);
		}
		var loadCurve = new LoadCurveSection();
		loadCurve.setSorted(!consumer().hasProfile());
		loadCurve.render(body, tk);
		new TransferStationSection(editor).create(body, tk);
		editor.onCalculated(
				(profile, totals, total) -> loadCurve.setData(profile));
		form.reflow(true);
		editor.calculate();
	}
}
