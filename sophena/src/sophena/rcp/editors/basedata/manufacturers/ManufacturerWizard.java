package sophena.rcp.editors.basedata.manufacturers;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import sophena.model.Manufacturer;
import sophena.rcp.Icon;
import sophena.rcp.colors.Colors;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Desktop;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Strings;

class ManufacturerWizard extends Wizard {

	private Page page;
	private Manufacturer manufacturer;

	static int open(Manufacturer manufacturer) {
		if (manufacturer == null)
			return Window.CANCEL;
		ManufacturerWizard w = new ManufacturerWizard();
		w.setWindowTitle("Hersteller");
		w.manufacturer = manufacturer;
		WizardDialog d = new WizardDialog(UI.shell(), w);
		return d.open();
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

		DataBinding data = new DataBinding();

		Text nameText;
		Text addressText;
		Text urlText;
		Text descriptionText;

		Page() {
			super("ManufacturerWizardPage", "Hersteller", null);
			setMessage(" ");
		}

		@Override
		public void createControl(Composite parent) {
			Composite c = new Composite(parent, SWT.NONE);
			setControl(c);
			UI.gridLayout(c, 3);

			nameText = UI.formText(c, "Name");
			Texts.on(nameText).required().validate(data::validate);
			UI.formLabel(c, "");

			addressText = UI.formMultiText(c, "Adresse");
			UI.formLabel(c, "");

			createWebLink(c);

			descriptionText = UI.formMultiText(c, "Zusatzinformation");
			UI.filler(c);

			data.bindToUI();
		}

		private void createWebLink(Composite c) {
			if (!manufacturer.isProtected) {
				urlText = UI.formText(c, "Web-Link");
				ImageHyperlink link = new ImageHyperlink(c, SWT.NONE);
				link.setImage(Icon.WEBLINK_16.img());
				Controls.onClick(link,
						e -> Desktop.browse(urlText.getText()));
				return;
			}
			UI.formLabel(c, "Web-Link");
			Hyperlink link = new Hyperlink(c, SWT.NONE);
			link.setForeground(Colors.getLinkBlue());
			if (manufacturer.url == null) {
				link.setText(""); // SWT throws a NullPointer otherwise
			} else {
				link.setText(Strings.cut(manufacturer.url, 60));
				link.setToolTipText(manufacturer.url);
			}
			UI.filler(c);
			Controls.onClick(link, e -> {
				Desktop.browse(manufacturer.url);
			});
		}

		private class DataBinding {

			void bindToUI() {
				Texts.set(nameText, manufacturer.name);
				Texts.set(addressText, manufacturer.address);
				if (urlText != null) {
					Texts.set(urlText, manufacturer.url);
				}
				Texts.set(descriptionText, manufacturer.description);

			}

			void bindToModel() {
				manufacturer.name = nameText.getText();
				manufacturer.address = addressText.getText();
				manufacturer.description = descriptionText.getText();
				if (urlText != null) {
					manufacturer.url = urlText.getText();
				}
			}

			boolean validate() {
				if (Texts.isEmpty(nameText))
					return error("Es muss ein Name angegeben werden.");
				else {
					setPageComplete(!manufacturer.isProtected);
					setErrorMessage(null);
					return true;
				}
			}

			boolean error(String message) {
				setErrorMessage(message);
				setPageComplete(false);
				return false;
			}
		}

	}

}
