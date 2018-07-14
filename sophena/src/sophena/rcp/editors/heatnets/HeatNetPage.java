package sophena.rcp.editors.heatnets;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.model.HeatNet;
import sophena.rcp.editors.LoadCurveSection;
import sophena.rcp.utils.UI;

class HeatNetPage extends FormPage {

	private HeatNetEditor editor;

	public HeatNetPage(HeatNetEditor editor) {
		super(editor, "sophena.HeatNetPage", "Wärmeverteilung");
		this.editor = editor;
	}

	private HeatNet heatNet() {
		return editor.heatNet;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		super.createFormContent(mform);
		ScrolledForm form = UI.formHeader(mform, "Wärmeverteilung");
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		new PipeSection(editor).create(body, tk);
		HeatNetSection netSection = new HeatNetSection(editor);
		netSection.create(body, tk);
		new BufferTankSection(editor).create(body, tk);
		InterruptionSection interruptionSec = new InterruptionSection(editor);
		interruptionSec.create(body, tk);
		LoadCurveSection loadCurve = createLoadCurve(tk, body);
		netSection.loadCurve = loadCurve;
		interruptionSec.setLoadCurve(loadCurve);
		form.reflow(true);
	}

	private LoadCurveSection createLoadCurve(FormToolkit toolkit,
			Composite body) {
		LoadCurveSection loadCurve = new LoadCurveSection();
		loadCurve.setSorted(false);
		loadCurve.setTitle("Verlustleistung des Wärmenetzes");
		loadCurve.render(body, toolkit);
		loadCurve.setData(NetLoadProfile.get(heatNet()));
		return loadCurve;
	}

}
