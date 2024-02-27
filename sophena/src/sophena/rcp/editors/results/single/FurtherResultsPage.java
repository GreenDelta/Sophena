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

import sophena.calc.CO2Result;
import sophena.math.energetic.EfficiencyResult;
import sophena.math.energetic.GeneratedHeat;
import sophena.math.energetic.PrimaryEnergyFactor;
import sophena.math.energetic.UsedHeat;
import sophena.model.Producer;
import sophena.rcp.M;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpLink;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class FurtherResultsPage extends FormPage {

	private final ResultEditor editor;

	FurtherResultsPage(ResultEditor editor) {
		super(editor, "sophena.EmissionsPage", M.FurtherResults);
		this.editor = editor;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, M.FurtherResults);
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		Function<String, Composite> s = title -> UI.formSection(body, tk,
				title);
		CO2Result co2 = editor.result.co2Result;
		EmissionTable.create(co2, s.apply("Treibhausgasemissionen"));
		EmissionChart.create(co2, body, tk);
		EfficiencyResult efficiency = EfficiencyResult.calculate(editor.result);
		EfficiencyTable.create(efficiency, s.apply("Effizienz"));
		EfficiencyChart.create(efficiency, body, tk);
		new KeyFigureTable().render(s.apply("Kennzahlen Wärmenetz"), tk);
		form.reflow(true);
	}

	private class KeyFigureTable {

		private void render(Composite comp, FormToolkit tk) {
			if (editor.project == null || editor.project.heatNet == null)
				return;
			UI.gridLayout(comp, 3);

			UI.formLabel(comp, tk, "Trassenlänge");
			double length = editor.project.heatNet.length;
			Label lengthLabel = UI.formLabel(comp, tk, Num.intStr(length));
			lengthLabel.setAlignment(SWT.RIGHT);
			lengthLabel.setLayoutData(
					new GridData(SWT.RIGHT, SWT.TOP, false, false));
			UI.formLabel(comp, tk, "m");

			UI.formLabel(comp, tk, "Wärmebelegungsdichte");
			double hl = length == 0 ? 0
					: UsedHeat.get(editor.result) / (1000 * length);
			Label hlLabel = UI.formLabel(comp, tk, Num.str(hl, 2));
			hlLabel.setAlignment(SWT.RIGHT);
			hlLabel.setLayoutData(
					new GridData(SWT.RIGHT, SWT.TOP, false, false));
			UI.formLabel(comp, tk, "MWh/(m*a)");

			UI.formLabel(comp, tk, "Primärenergiefaktor");
			double pef = PrimaryEnergyFactor.get(editor.result);
			Label pefLabel = UI.formLabel(comp, tk, Num.str(pef, 2));
			pefLabel.setAlignment(SWT.RIGHT);
			pefLabel.setLayoutData(
					new GridData(SWT.RIGHT, SWT.TOP, false, false));
			HelpLink.create(comp, tk, "Primärenergiefaktor",
					H.PrimaryEnergyFactor);			
					
			int share = 0;
			for (Producer p : editor.project.producers)
			{
				if(!p.disabled && p.solarCollector != null && p.solarCollectorSpec != null)
				{
					share += GeneratedHeat.share(editor.result.energyResult.totalHeat(p), editor.result.energyResult);					
				}
			}
			if (share > 0)
			{
				UI.formLabel(comp, tk, "Solarthermischer Deckungsbeitrag");
				Label shareLabel = UI.formLabel(comp, tk, Num.intStr(share));
				shareLabel.setAlignment(SWT.RIGHT);
				shareLabel.setLayoutData(
						new GridData(SWT.RIGHT, SWT.TOP, false, false));
				UI.formLabel(comp, tk, "%");
			}
			UI.formLabel(comp, tk, "");
		}
	}
}
