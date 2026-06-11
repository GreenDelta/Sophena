package sophena.rcp.editors.biogas.plant;

import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.calc.biogas.BiogasPlantResult;
import sophena.model.Stats;
import sophena.rcp.charts.Charts;
import sophena.rcp.colors.ColorConfig;
import sophena.rcp.colors.ColorKey;
import sophena.rcp.colors.Colors;
import sophena.rcp.utils.UI;

class ProducerProfileSection {

	private final BiogasPlantEditor editor;
	private XYGraph graph;
	private CircularBufferDataProvider data;

	private ProducerProfileSection(BiogasPlantEditor editor) {
		this.editor = editor;
	}

	static ProducerProfileSection of(BiogasPlantEditor editor) {
		return new ProducerProfileSection(editor);
	}

	void create(Composite body, FormToolkit tk) {
		var section = UI.section(body, tk, "Erzeugerlastgang");
		var root = UI.sectionClient(section, tk);
		UI.gridLayout(root, 1);

		graph = Charts.initHoursGraph(root, 250);
		graph.getPrimaryYAxis().setTitle("kW");
		data = Charts.dataProvider();
		var color = Colors.of(
				ColorConfig.get().get(ColorKey.PRODUCER_PROFILE));
		var trace = Charts.lineTraceOf(graph, "Max", color, data);
		trace.setTraceType(TraceType.STEP_VERTICALLY);
		editor.onResult(this::setInput);
	}

	private void setInput(BiogasPlantResult r) {
		var profile = r.asProducerProfile(90);
		double[] nums = profile.maxPower;
		if (nums == null)
			return;
		data.setCurrentYDataArray(nums);
		double top = Stats.nextStep(Stats.max(nums));
		graph.getPrimaryYAxis().setRange(0, top);
	}
}
