package sophena.rcp.editors.biogas.electricity;

import java.io.File;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.model.biogas.ElectricityPriceCurve;
import sophena.rcp.M;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Strings;

public class ElectricityPriceWizard extends Wizard {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private Page page;
	private final ElectricityPriceCurve curve;

	public static int open(ElectricityPriceCurve curve) {
		if (curve == null)
			return Window.CANCEL;
		var wiz = new ElectricityPriceWizard(curve);
		wiz.setWindowTitle("Strompreiskurve");
		var dialog = new WizardDialog(UI.shell(), wiz);
		return dialog.open();
	}

	private ElectricityPriceWizard(ElectricityPriceCurve curve) {
		super();
		this.curve = curve;
	}

	@Override
	public boolean performFinish() {
		try {

			var name = page.nameText.getText();
			if (Strings.nullOrEmpty(name)) {
				MsgBox.error("Kein Name angegen", "Der Name darf nicht leer sein.");
				return false;
			}

			if (curve.values == null && page.file == null) {
				MsgBox.error("Keine Datei ausgewählt",
						"Bitte wählen Sie eine Datei mit Strompreisen aus.");
				return false;
			}

			if (page.file != null) {
				var data = ElectricityPriceIO.read(page.file).orElse(null);
				if (data == null)
					return false;
				curve.values = data;
			}
			curve.name = name;
			curve.description = page.descriptionText.getText();
			return true;

		} catch (Exception e) {
			log.error("failed to set electricity price curve data {}", curve, e);
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
		private File file;

		Page() {
			super("ElectricityPriceWizardPage", "Strompreiskurve", null);
		}

		@Override
		public void createControl(Composite parent) {
			var comp = new Composite(parent, SWT.NONE);
			setControl(comp);
			UI.gridLayout(comp, 3);

			nameText = UI.formText(comp, M.Name);
			Texts.on(nameText)
					.init(curve.name)
					.required();
			UI.filler(comp);

			descriptionText = UI.formMultiText(comp, M.Description);
			Texts.on(descriptionText).init(curve.description);
			UI.filler(comp);

			var text = UI.formText(comp, "Datei");
			text.setEditable(false);
			var button = new Button(comp, SWT.PUSH);
			button.setText("Durchsuchen...");

			Controls.onSelect(button, $ -> {
				var f = FileChooser.open("*.xlsx");
				if (f == null)
					return;
				file = f;
				text.setText(file.getName());
			});
		}
	}
}
