package sophena.rcp.editors.basedata.buildings;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.model.BuildingState;
import sophena.model.BuildingType;
import sophena.rcp.Labels;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class BuildingStateWizard extends Wizard {

	private Page page;
	private BuildingState state;

	public static int open(BuildingState state) {
		if (state == null)
			return Window.CANCEL;
		BuildingStateWizard wiz = new BuildingStateWizard();
		wiz.setWindowTitle("Gebäudezustand");
		wiz.state = state;
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		dialog.setPageSize(150, 400);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		try {
			page.data.bintToModel();
			return true;
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to bind data to model", e);
			return false;
		}
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		DataBinding data = new DataBinding();

		Combo typeCombo;
		Text nameText;
		Text indexText;
		Text heatingLimitText;
		Text waterFractionText;
		Text loadHoursText;
		Button isDefaultCheck;

		Page() {
			super("BuildingStatePage", "Gebäudezustand", null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite c = new Composite(parent, SWT.NONE);
			UI.gridLayout(c, 3);
			setControl(c);
			createTypeCombo(c);
			nameText = UI.formText(c, "Gebäudezustand");
			UI.filler(c);
			indexText = UI.formText(c, "Index");
			UI.filler(c);
			heatingLimitText = UI.formText(c, "Heizgrenztemperatur");
			UI.formLabel(c, "°C");
			waterFractionText = UI.formText(c, "Warmwasseranteil");
			UI.formLabel(c, "%");
			loadHoursText = UI.formText(c, "Volllaststunden");
			UI.formLabel(c, "h");
			isDefaultCheck = UI.formCheckBox(c, "Voreinstellung");
			UI.filler(c);
			data.bindToUI();
		}

		void createTypeCombo(Composite c) {
			typeCombo = UI.formCombo(c, "Gebäudetyp");
			BuildingType[] types = BuildingType.values();
			String[] items = new String[types.length];
			for (int i = 0; i < types.length; i++) {
				items[i] = Labels.get(types[i]);
			}
			typeCombo.setItems(items);
			UI.filler(c);
		}

		private class DataBinding {

			void bindToUI() {
				typeToUI();
				Texts.on(nameText).required().init(state.name);
				Texts.on(indexText).required().integer().init(state.index);
				Texts.on(heatingLimitText).required().decimal()
						.init(state.heatingLimit);
				Texts.on(waterFractionText).required().decimal()
						.init(state.waterFraction);
				Texts.on(loadHoursText).required().integer()
						.init(state.loadHours);
				isDefaultCheck.setSelection(state.isDefault);
			}

			void typeToUI() {
				String label = state.type != null
						? Labels.get(state.type) : Labels.get(BuildingType.OTHER);
				int select = 0;
				String[] items = typeCombo.getItems();
				for (int i = 0; i < items.length; i++) {
					if (Strings.nullOrEqual(label, items[i])) {
						select = i;
						break;
					}
				}
				typeCombo.select(select);
			}

			void bintToModel() {
				int idx = typeCombo.getSelectionIndex();
				String item = typeCombo.getItem(idx);
				state.type = Labels.getBuildingType(item);
				state.name = nameText.getText();
				state.index = Texts.getInt(indexText);
				state.isDefault = isDefaultCheck.getSelection();
				state.heatingLimit = Texts.getDouble(heatingLimitText);
				state.waterFraction = Texts.getDouble(waterFractionText);
				state.loadHours = Texts.getInt(loadHoursText);
			}
		}
	}
}
