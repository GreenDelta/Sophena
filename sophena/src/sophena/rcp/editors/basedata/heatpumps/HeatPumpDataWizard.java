package sophena.rcp.editors.basedata.heatpumps;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sophena.rcp.M;
import sophena.rcp.editors.basedata.heatpumps.HeatPumpWizard.HeatPumpData;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

public class HeatPumpDataWizard extends Wizard {
	
	private HeatPumpData heatPumpData;

	private Page page;
	
	private Text maxText;
	private Text targetText;
	private Text sourceText;
	private Text copText;

	public static int open(HeatPumpData heatPumpData)
	{
		if (heatPumpData == null)
			return Window.CANCEL;
		HeatPumpDataWizard w = new HeatPumpDataWizard();
		w.heatPumpData = heatPumpData;
		w.setWindowTitle("Betriebspunkte");
		WizardDialog dialog = new WizardDialog(UI.shell(), w);
		return dialog.open();
	}
	@Override
	public boolean performFinish() {

		return true;
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		Page() {
			super("OverviewPage", "Betriebspunkte", null);
			setMessage(" ");
		}

		@Override
		public void createControl(Composite parent) {
			Composite comp = new Composite(parent, SWT.NONE);
			setControl(comp);
			UI.gridLayout(comp, 3);
			CreateTexts(comp);
		}
		
		private void CreateTexts(Composite comp)
		{
			targetText = UI.formText(comp, M.TargetTemperature);
			Texts.on(targetText).init(heatPumpData.targetTemperature).decimal().required()
						.onChanged(s -> {
							heatPumpData.targetTemperature = 5*(Math.round(Texts.getInt(targetText)/5));
						});
			UI.formLabel(comp, "°C");
			
			sourceText = UI.formText(comp, M.SourceTemperature);
			Texts.on(sourceText).init(heatPumpData.sourceTemperature).decimal().required()
			.onChanged(s -> {
				heatPumpData.sourceTemperature = 5*(Math.round(Texts.getInt(sourceText)/5));
			});
			UI.formLabel(comp, "°C");
			
			maxText = UI.formText(comp, M.MaxPower);
			Texts.on(maxText).init(heatPumpData.maxPower).decimal().required()
			.onChanged(s -> {
				heatPumpData.maxPower = Texts.getDouble(maxText);
			});
			UI.formLabel(comp, "kW");
			
			copText = UI.formText(comp, M.Cop);
			Texts.on(copText).init(heatPumpData.cop).decimal().required()
			.onChanged(s -> {
				heatPumpData.cop = Texts.getDouble(copText);
			});
			UI.formLabel(comp, "");
		}
	}
}
