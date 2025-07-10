package sophena.rcp.editors.biogas.electricity;

import java.io.File;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.model.Stats;
import sophena.model.biogas.ElectricityPriceCurve;
import sophena.rcp.M;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Ref;
import sophena.utils.Strings;

public class ElectricityPriceWizard extends Wizard {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private Page page;
	private ElectricityPriceCurve curve;

	public static int open(ElectricityPriceCurve curve) {
		if (curve == null)
			return Window.CANCEL;
		ElectricityPriceWizard wiz = new ElectricityPriceWizard();
		wiz.setWindowTitle("Strompreiskurve");
		wiz.curve = curve;
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		try {


			curve.name = page.nameText.getText();
			curve.description = page.descriptionText.getText();

			var file = page.excelPanel.file();
			if (file != null) {
				var values = ElectricityPriceIO.read(file);
				if (values.isPresent()) {
					curve.values = values.get();
				} else {
					// Initialize empty values if reading failed
					curve.values = new double[Stats.HOURS];
				}
			} else if (curve.values == null) {
				// Initialize empty values if no file selected
				curve.values = new double[Stats.HOURS];
			}

			return true;
		} catch (Exception e) {
			log.error("failed to set electricity price curve data " + curve, e);
			return false;
		}
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private Text nameText;
		private Text descriptionText;
		private ExcelPanel excelPanel;

		Page() {
			super("ElectricityPriceWizardPage", "Strompreiskurve", null);
			setMessage("Geben Sie die Informationen für die Strompreiskurve ein");
		}

		@Override
		public void createControl(Composite parent) {
			var comp = new Composite(parent, SWT.NONE);
			setControl(comp);
			UI.gridLayout(comp, 1);
			createBasicFields(comp);
			createExcelSection(comp);
			bindToUI();
		}

		private void createBasicFields(Composite parent) {
			Group group = new Group(parent, SWT.NONE);
			group.setText("Grunddaten");
			UI.gridData(group, true, false);
			UI.gridLayout(group, 2);

			nameText = UI.formText(group, M.Name);
			Texts.on(nameText).required();
			nameText.addModifyListener(e -> validate());

			descriptionText = UI.formMultiText(group, M.Description);
		}

		private void createExcelSection(Composite parent) {
			excelPanel = ExcelPanel.create(parent);
		}

		private void bindToUI() {
			if (curve.name != null) {
				nameText.setText(curve.name);
			}
			if (curve.description != null) {
				descriptionText.setText(curve.description);
			}
			validate();
		}

		private void validate() {
			if (Strings.nullOrEmpty(nameText.getText())) {
				setErrorMessage("Bitte geben Sie einen Namen ein");
				setPageComplete(false);
				return;
			}
			setErrorMessage(null);
			setPageComplete(true);
		}

		private record ExcelPanel(Text text, Button button, Ref<File> fileRef) {

			static ExcelPanel create(Composite comp) {
				var group = new Group(comp, SWT.NONE);
				group.setText("Excel-Datei mit Strompreisen");
				UI.gridData(group, true, false);
				UI.gridLayout(group, 3);

				UI.formLabel(group, "Datei (optional):");
				var text = UI.formText(group, "");
				text.setEditable(false);

				var button = new Button(group, SWT.PUSH);
				button.setText("Durchsuchen...");

				var ref = new Ref<File>();
				Controls.onSelect(button, $ -> {
					var file = FileChooser.open("*.xlsx");
					if (file == null)
						return;
					text.setText(file.getName());
					ref.set(file);
				});

				return new ExcelPanel(text, button, ref);
			}

			File file() {
				return fileRef != null ? fileRef.get() : null;
			}
		}
	}
}
