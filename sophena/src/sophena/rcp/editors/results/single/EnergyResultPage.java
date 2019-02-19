package sophena.rcp.editors.results.single;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.calc.EnergyResult;
import sophena.calc.ProjectLoad;
import sophena.model.LoadProfile;
import sophena.model.Project;
import sophena.model.Stats;
import sophena.rcp.M;
import sophena.rcp.editors.LoadCurveSection;
import sophena.rcp.utils.UI;

class EnergyResultPage extends FormPage {

	private final ResultEditor editor;
	private EnergyResult result;
	private double maxLoad;

	EnergyResultPage(ResultEditor editor) {
		super(editor, "sophena.EnergyResultPage", M.Heat);
		this.editor = editor;
		this.result = editor.result.energyResult;
		Project p = editor.project;
		maxLoad = ProjectLoad.getSimultaneousMax(p);
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, M.Heat);
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		BoilerTableSection tableSection = new BoilerTableSection(editor,
				maxLoad);
		tableSection.render(body, tk);
		BoilerChart unsortedChart = new BoilerChart(result, maxLoad);
		unsortedChart.setSorted(false);
		unsortedChart.render(body, tk);
		BoilerChart sortedChart = new BoilerChart(result, maxLoad);
		sortedChart.setSorted(true);
		sortedChart.render(body, tk);
		createLoadCurve(tk, body);
		form.reflow(true);
	}

	private void createLoadCurve(FormToolkit tk, Composite body) {
		LoadCurveSection section = new LoadCurveSection();
		section.setTitle("Jahresdauerlinie - Netzlast");
		section.setSorted(true);
		LoadProfile profile = new LoadProfile();
		profile.dynamicData = Stats.copy(result.loadCurve);
		profile.staticData = new double[Stats.HOURS];
		section.setData(profile);
		section.render(body, tk);
	}
}
