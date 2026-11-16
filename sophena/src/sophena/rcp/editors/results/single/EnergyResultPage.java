package sophena.rcp.editors.results.single;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.calc.simulation.EnergyResult;
import sophena.model.LoadProfile;
import sophena.rcp.M;
import sophena.rcp.colors.ResultColors;
import sophena.rcp.editors.LoadCurveSection;
import sophena.rcp.utils.UI;

class EnergyResultPage extends FormPage {

	private final ResultEditor editor;
	private final ResultColors colors;
	private final EnergyResult result;
	private final double maxLoad;

	EnergyResultPage(ResultEditor editor) {
		super(editor, "sophena.EnergyResultPage", M.Heat);
		this.editor = editor;
		this.colors = editor.colors;
		this.result = editor.result.energyResult;
		maxLoad = result.load.simultaneousMax();
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		var form = UI.formHeader(mform, M.Heat);
		var tk = mform.getToolkit();
		var body = UI.formBody(form, tk);
		new BoilerTableSection(editor, maxLoad)
				.render(body, tk);
		new BoilerChart(result, colors,  maxLoad)
				.sorted(false)
				.render(body, tk);
		new BoilerChart(result, colors, maxLoad)
				.sorted(true)
				.render(body, tk);
		createLoadCurve(tk, body);
		form.reflow(true);
	}

	private void createLoadCurve(FormToolkit tk, Composite body) {
		var section = new LoadCurveSection();
		section.setTitle("Jahresdauerlinie - Netzlast");
		section.setSorted(true);
		var profile = new LoadProfile();
		profile.dynamicData = result.load.dynamicCurve();
		profile.staticData = result.load.staticCurve();
		section.setData(profile);
		section.render(body, tk);
	}
}
