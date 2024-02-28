package sophena.rcp.editors.results.single;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import sophena.calc.EnergyResult;
import sophena.calc.ProjectLoad;
import sophena.model.LoadProfile;
import sophena.model.Project;
import sophena.model.Stats;
import sophena.rcp.M;
import sophena.rcp.colors.ResultColors;
import sophena.rcp.editors.LoadCurveSection;
import sophena.rcp.utils.UI;

class EnergyResultPage extends FormPage {

	private final ResultEditor editor;
	private final ResultColors colors;
	private final EnergyResult result;
	private final double maxLoad;
	private final double maxPeakPower;

	EnergyResultPage(ResultEditor editor) {
		super(editor, "sophena.EnergyResultPage", M.Heat);
		this.editor = editor;
		this.colors = editor.colors;
		this.result = editor.result.energyResult;
		Project p = editor.project;
		maxLoad = ProjectLoad.getSimultaneousMax(p);
		maxPeakPower = editor.result.energyResult.maxPeakPowerOfAllProducers();
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		var form = UI.formHeader(mform, M.Heat);
		var tk = mform.getToolkit();
		var body = UI.formBody(form, tk);
		new BoilerTableSection(editor, maxLoad)
				.render(body, tk);
		new BoilerChart(result, colors, Math.max(maxPeakPower, maxLoad))
				.sorted(false)
				.render(body, tk);
		new BoilerChart(result, colors, Math.max(maxPeakPower, maxLoad))
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
		profile.dynamicData = Stats.copy(result.loadCurve);
		profile.staticData = new double[Stats.HOURS];
		section.setData(profile);
		section.render(body, tk);
	}
}
