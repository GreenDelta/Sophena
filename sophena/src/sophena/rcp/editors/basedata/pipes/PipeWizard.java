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
		d.setPageSize(150, 560);
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

		EntityCombo<ProductGroup> groupCombo;
		Text nameText;
		Text manufacturerText;
		Text urlText;
		Text priceText;
		Text materialText;
		Text pTypeText;
		Text uValueText;
		Text innerDiamText;
		Text outerDiamText;
		Text totalDiamText;
		Text deliveryTypeText;
		Text maxTempText;
		Text maxPressureText;
		Text descriptionText;

		Page() {
			super("PipeWizardPage", "Wärmeleitung", null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite c = new Composite(parent, SWT.NONE);
			setControl(c);
			UI.gridLayout(c, 3);

			createGroupCombo(c);

			nameText = UI.formText(c, M.Name);
			Texts.on(nameText).required().validate(data::validate);
			UI.formLabel(c, "");

			manufacturerText = UI.formText(c, "Hersteller");
			Texts.on(manufacturerText).required().validate(data::validate);
			UI.formLabel(c, "");

			urlText = UI.formText(c, "Web-Link");
			Texts.on(urlText).required().validate(data::validate);
			UI.formLabel(c, "");

			priceText = UI.formText(c, "Preis");
			UI.formLabel(c, "EUR/m");

			materialText = UI.formText(c, "Material");
			Texts.on(materialText).required().validate(data::validate);
			UI.formLabel(c, "");

			pTypeText = UI.formText(c, "Art");
			Texts.on(pTypeText).required().validate(data::validate);
			UI.formLabel(c, "");

			uValueText = UI.formText(c, "U-Wert");
			Texts.on(uValueText).required().decimal().validate(data::validate);
			UI.formLabel(c, "W/(m².K)");

			innerDiamText = UI.formText(c, "Innend. Medienrohr");
			Texts.on(innerDiamText).required().decimal().validate(data::validate);
			UI.formLabel(c, "mm");

			outerDiamText = UI.formText(c, "Außend. Medienrohr");
			Texts.on(outerDiamText).required().decimal().validate(data::validate);
			UI.formLabel(c, "mm");

			totalDiamText = UI.formText(c, "Außend. Gesamt");
			Texts.on(totalDiamText).required().decimal().validate(data::validate);
			UI.formLabel(c, "mm");

			deliveryTypeText = UI.formText(c, "Lieferausführung");
			UI.formLabel(c, "");

			maxTempText = UI.formText(c, "Maximale Temperatur");
			UI.formLabel(c, "°C");

			maxPressureText = UI.formText(c, "Maximaler Druck");
			UI.formLabel(c, "Bar");

			descriptionText = UI.formMultiText(c, "Zusatzinformation");
			UI.filler(c);

			data.bindToUI();
		}

		private void createGroupCombo(Composite c) {
			groupCombo = new EntityCombo<>();
			groupCombo.create("Produktgruppe", c);
			ProductGroupDao dao = new ProductGroupDao(App.getDb());
			List<ProductGroup> list = dao.getAll(ProductType.PIPE);
			Sorters.byName(list);
			groupCombo.setInput(list);
			UI.formLabel(c, "");
		}

		private class DataBinding {

			void bindToUI() {
				Texts.set(nameText, pipe.name);
				groupCombo.select(pipe.group);
				Texts.set(totalDiamText, pipe.totalDiameter);
				Texts.set(uValueText, pipe.uValue);
				Texts.set(urlText, pipe.url);
				Texts.set(priceText, pipe.purchasePrice);
				Texts.set(materialText, pipe.material);
				// Texts.set(pTypeText, pipe.pipeType);
				Texts.set(innerDiamText, pipe.innerDiameter);
				Texts.set(outerDiamText, pipe.outerDiameter);
				Texts.set(totalDiamText, pipe.totalDiameter);
				Texts.set(deliveryTypeText, pipe.deliveryType);
				Texts.set(maxTempText, pipe.maxTemperature);
				Texts.set(maxPressureText, pipe.maxPressure);
				Texts.set(descriptionText, pipe.description);

			}

			void bindToModel() {
				pipe.name = nameText.getText();
				pipe.group = groupCombo.getSelected();
				pipe.material = materialText.getText();
				// pipe.pipeType. = pTypeText.getText();
				pipe.totalDiameter = Texts.getDouble(totalDiamText);
				pipe.innerDiameter = Texts.getDouble(innerDiamText);
				pipe.outerDiameter = Texts.getDouble(outerDiamText);
				pipe.maxTemperature = Texts.getDouble(maxTempText);
				pipe.maxPressure = Texts.getDouble(maxPressureText);
				pipe.outerDiameter = Texts.getDouble(outerDiamText);
				pipe.deliveryType = deliveryTypeText.getText();
				pipe.description = descriptionText.getText();
				pipe.uValue = Texts.getDouble(uValueText);
				pipe.url = urlText.getText();
				if (Texts.hasNumber(priceText))
					pipe.purchasePrice = Texts.getDouble(priceText);
			}

			boolean validate() {
				if (Texts.isEmpty(nameText))
					return error("Es muss ein Name angegeben werden.");
				if (!Texts.hasNumber(totalDiamText))
					return error("Es muss ein Durchmesser angegeben werden.");
				if (!Texts.hasNumber(uValueText))
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
