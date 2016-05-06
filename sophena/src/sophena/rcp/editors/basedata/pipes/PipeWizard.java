package sophena.rcp.editors.basedata.pipes;

import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import sophena.db.daos.ProductGroupDao;
import sophena.model.Pipe;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.rcp.App;
import sophena.rcp.M;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class PipeWizard extends Wizard {

	private Page page;
	private Pipe pipe;

	static int open(Pipe pipe) {
		if (pipe == null)
			return Window.CANCEL;
		PipeWizard w = new PipeWizard();
		w.setWindowTitle("Wärmeleitung");
		w.pipe = pipe;
		WizardDialog d = new WizardDialog(UI.shell(), w);
		d.setPageSize(150, 400);
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
		EntityCombo<ProductGroup> groupCombo;
		Text diamText;
		Text uText;
		Text urlText;
		Text priceText;

		Page() {
			super("PipeWizardPage", "Wärmeleitung", null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite c = new Composite(parent, SWT.NONE);
			setControl(c);
			UI.gridLayout(c, 3);

			nameText = UI.formText(c, M.Name);
			Texts.on(nameText).required().validate(data::validate);
			UI.formLabel(c, "");

			createGroupCombo(c);

			diamText = UI.formText(c, "Durchmesser");
			Texts.on(diamText).required().decimal().validate(data::validate);
			UI.formLabel(c, "mm");

			uText = UI.formText(c, "U-Wert");
			Texts.on(uText).required().decimal().validate(data::validate);
			UI.formLabel(c, "W/(m².K)");

			urlText = UI.formText(c, "Web-Link");
			UI.formLabel(c, "");

			priceText = UI.formText(c, "Preis");
			UI.formLabel(c, "EUR/m");

			data.bindToUI();
		}

		private void createGroupCombo(Composite c) {
			groupCombo = new EntityCombo<>();
			groupCombo.create("Produktgruppe", c);
			ProductGroupDao dao = new ProductGroupDao(App.getDb());
			List<ProductGroup> list = dao
					.getAll(ProductType.HEATING_NET_TECHNOLOGY);
			Sorters.byName(list);
			groupCombo.setInput(list);
			UI.formLabel(c, "");
		}

		private class DataBinding {

			void bindToUI() {
				Texts.set(nameText, pipe.name);
				groupCombo.select(pipe.group);
				Texts.set(diamText, pipe.totalDiameter);
				Texts.set(uText, pipe.uValue);
				Texts.set(urlText, pipe.url);
				Texts.set(priceText, pipe.purchasePrice);
			}

			void bindToModel() {
				pipe.name = nameText.getText();
				pipe.group = groupCombo.getSelected();
				pipe.totalDiameter = Texts.getDouble(diamText);
				pipe.uValue = Texts.getDouble(uText);
				pipe.url = urlText.getText();
				if (Texts.hasNumber(priceText))
					pipe.purchasePrice = Texts.getDouble(priceText);
			}

			boolean validate() {
				if (Texts.isEmpty(nameText))
					return error("Es muss ein Name angegeben werden.");
				if (!Texts.hasNumber(diamText))
					return error("Es muss ein Durchmesser angegeben werden.");
				if (!Texts.hasNumber(uText))
					return error("Es muss ein U-Wert angegeben werden.");
				else {
					setPageComplete(true);
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
