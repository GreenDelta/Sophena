package sophena.rcp.editors.results.single;

import java.util.function.Function;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.math.energetic.CO2Emissions;
import sophena.math.energetic.EfficiencyResult;
import sophena.rcp.M;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class FurtherResultsPage extends FormPage {

	private ResultEditor editor;

	FurtherResultsPage(ResultEditor editor) {
		super(editor, "sophena.EmissionsPage", M.FurtherResults);
		this.editor = editor;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, M.FurtherResults);
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		Function<String, Composite> s = title -> UI.formSection(body, tk, title);
		CO2Emissions emissions = CO2Emissions.calculate(editor.result);
		EmissionTable.create(emissions, s.apply("Treibhausgasemissionen"));
		EmissionChart.create(emissions, s.apply("Vergleich Treibhausgasemissionen"));
		EfficiencyResult efficiency = EfficiencyResult.calculate(editor.result);
		EfficiencyTable.create(efficiency, s.apply("Effizienz"));
		EfficiencyChart.create(efficiency, s.apply("Verwendung Brennstoffenergie"));
		new KeyFigureTable(efficiency).render(s.apply("Kennzahlen Wärmenetz"), tk);
		form.reflow(true);
	}

	private class KeyFigureTable {

		private EfficiencyResult er;

		KeyFigureTable(EfficiencyResult er) {
			this.er = er;
		}

		private void render(Composite comp, FormToolkit tk) {
			if (editor.project == null || editor.project.heatNet == null)
				return;
			UI.gridLayout(comp, 3);

			UI.formLabel(comp, tk, "Trassenlänge");
			double length = editor.project.heatNet.length;
			Label lengthLabel = UI.formLabel(comp, tk, Num.intStr(length));
			lengthLabel.setAlignment(SWT.RIGHT);
			lengthLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			UI.formLabel(comp, tk, "m");

			UI.formLabel(comp, tk, "Wärmebelegungsdichte");
			double hl = er.usedHeat / (1000 * length);
			Label hlLabel = UI.formLabel(comp, tk, Num.str(hl));
			hlLabel.setAlignment(SWT.RIGHT);
			hlLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
			UI.formLabel(comp, tk, "MWh/(m*a)");
		}
	}
}
