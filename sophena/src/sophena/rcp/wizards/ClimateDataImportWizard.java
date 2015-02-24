package sophena.rcp.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.utils.UI;

public class ClimateDataImportWizard extends Wizard {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Page page;

	public static void open() {
		ClimateDataImportWizard wiz = new ClimateDataImportWizard();
		wiz.setWindowTitle(M.ClimateData);
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		dialog.setPageSize(150, 400);
		dialog.open();
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

		private Page() {
			super("ClimateDataImportWizardPage", M.ClimateData, null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite comp = new Composite(parent, SWT.NONE);
			setControl(comp);
			UI.gridLayout(comp, 3);
			UI.formText(comp, "#Datumsspalte");
			UI.formLabel(comp, "");
			UI.formText(comp, "#Temperaturspalte");
			UI.formLabel(comp, "");
			UI.formText(comp, "#Spaltentrennzeichen");
			UI.formLabel(comp, "");
			UI.formText(comp, "#Startjahr");
			UI.formLabel(comp, "");
			UI.formText(comp, "#Endjahr");
			UI.formLabel(comp, "");
			UI.formText(comp, "#Datei");
			Button button = new Button(comp, SWT.NONE);
			button.setImage(Images.FILE_16.img());
			button.setText("#Auswählen");
		}
	}
}
