package sophena.rcp.editors.consumers;

import java.io.File;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sophena.io.LoadProfileReader;
import sophena.model.LoadProfile;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.charts.LoadProfileChart;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class LoadProfileWizard extends Wizard {

	private Page page;
	private LoadProfile profile;

	public static int open(LoadProfile profile) {
		if (profile == null)
			return Window.CANCEL;
		LoadProfileWizard wiz = new LoadProfileWizard();
		wiz.setWindowTitle(M.LoadProfile);
		wiz.profile = profile;
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		dialog.setPageSize(150, 400);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		page.data.bindToModel();
		return true;
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private DataBinding data = new DataBinding();

		private Text nameText;
		private Text descriptionText;
		private LoadProfileChart chart;

		private Page() {
			super("LoadProfileWizardPage", M.LoadProfile, null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			setControl(composite);
			UI.gridLayout(composite, 2);
			nameText = UI.formText(composite, M.Name);
			Texts.on(nameText).required().validate(data::validate);
			descriptionText = UI.formMultiText(composite, M.Description);
			createFileSection(composite);
			createChart(composite);
			data.bindToUI();
		}

		private void createFileSection(Composite composite) {
			UI.formLabel(composite, "");
			Button button = new Button(composite, SWT.NONE);
			button.setImage(Icon.FILE_16.img());
			button.setText(M.SelectFile);
			Controls.onSelect(button, (e) -> data.readFile());
		}

		private void createChart(Composite composite) {
			UI.formLabel(composite, "");
			Composite chartParent = new Composite(composite, SWT.NONE);
			UI.gridData(chartParent, true, true);
			UI.gridLayout(chartParent, 1);
			chart = new LoadProfileChart(chartParent, 150);
		}

		private class DataBinding {

			LoadProfile fileProfile;

			void bindToUI() {
				Texts.set(nameText, profile.name);
				Texts.set(descriptionText, profile.description);
				chart.setData(profile);
			}

			void readFile() {
				File f = FileChooser.open("*.csv", "*.txt");
				if (f == null)
					return;
				LoadProfileReader reader = new LoadProfileReader();
				fileProfile = reader.read(f);
				chart.setData(fileProfile);
			}

			void bindToModel() {
				profile.name = nameText.getText();
				profile.description = descriptionText.getText();
				if (fileProfile != null) {
					profile.dynamicData = fileProfile.dynamicData;
					profile.staticData = fileProfile.staticData;
				}
			}

			boolean validate() {
				if (Texts.isEmpty(nameText))
					return error("Es wurde kein Name eingetragen");
				else {
					setPageComplete(true);
					setErrorMessage(null);
					return true;
				}
			}

			private boolean error(String message) {
				setPageComplete(false);
				setErrorMessage(message);
				return false;
			}
		}
	}
}
