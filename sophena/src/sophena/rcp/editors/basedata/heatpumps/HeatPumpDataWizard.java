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
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

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
		if (!page.valid())
			return false;	
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
							heatPumpData.targetTemperature = Texts.getDouble(targetText);
						});
			UI.formLabel(comp, "°C");
			
			sourceText = UI.formText(comp, M.SourceTemperature);
			Texts.on(sourceText).init(heatPumpData.sourceTemperature).decimal().required()
			.onChanged(s -> {
				heatPumpData.sourceTemperature = Texts.getDouble(sourceText);
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
		
		private boolean valid() {
			if (heatPumpData.sourceTemperature < -30 || heatPumpData.sourceTemperature > 100) {
				MsgBox.error(M.PlausibilityErrors, M.SourceTemperatureError);
				return false;
			}	
			if (heatPumpData.targetTemperature % 5 != 0 || heatPumpData.targetTemperature < 5 || heatPumpData.targetTemperature > 95) {
				MsgBox.error(M.PlausibilityErrors, M.TargetTemperatureError);
				return false;
			}
			if (heatPumpData.maxPower <= 0) {
				MsgBox.error(M.PlausibilityErrors, M.MaxPowerError);
				return false;
			}
			if (heatPumpData.cop < 1 || heatPumpData.cop > 10) {
				MsgBox.error(M.PlausibilityErrors, M.COPError);
				return false;
			}

			return true;
		}
	}
}
