package sophena.rcp.editors.basedata.buildings;

import java.util.List;
import java.util.Objects;

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
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Strings;

class StateWizard extends Wizard {

	private Page page;
	private BuildingState state;
	private List<BuildingState> states;

	public static int open(BuildingState state, List<BuildingState> states) {
		if (state == null || states == null)
			return Window.CANCEL;
		StateWizard wiz = new StateWizard();
		wiz.setWindowTitle("Gebäudezustand");
		wiz.state = state;
		wiz.states = states;
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
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
		Text antifreezingText;
		Text waterFractionText;
		Text loadHoursText;
		Button isDefaultCheck;

		Page() {
			super("BuildingStatePage", "Gebäudezustand", null);
			setMessage(" ");
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
			antifreezingText = UI.formText(c, "Frostschutztemperatur");
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
				Controls.onSelect(typeCombo, e -> validate());
				Texts.on(nameText).required().init(state.name);
				Texts.on(indexText).required()
						.integer().init(state.index)
						.validate(() -> validate());
				Texts.on(heatingLimitText).required()
						.decimal().init(state.heatingLimit);
				Texts.on(antifreezingText).required()
						.decimal().init(state.antifreezingTemperature);
				Texts.on(waterFractionText).required()
						.decimal().init(state.waterFraction);
				Texts.on(loadHoursText).required()
						.integer().init(state.loadHours);
				isDefaultCheck.setSelection(state.isDefault);
				Controls.onSelect(isDefaultCheck, e -> validate());
				validate();
			}

			void typeToUI() {
				String label = state.type != null
						? Labels.get(state.type)
						: Labels.get(BuildingType.OTHER);
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
				state.type = getType();
				state.name = nameText.getText();
				state.index = Texts.getInt(indexText);
				state.isDefault = isDefaultCheck.getSelection();
				state.heatingLimit = Texts.getDouble(heatingLimitText);
				state.antifreezingTemperature = Texts
						.getDouble(antifreezingText);
				state.waterFraction = Texts.getDouble(waterFractionText);
				state.loadHours = Texts.getInt(loadHoursText);
			}

			boolean validate() {
				BuildingType type = getType();
				int idx = Texts.getInt(indexText);
				boolean isDefault = isDefaultCheck.getSelection();
				for (BuildingState other : states) {
					if (type != other.type || Objects.equals(state, other))
						continue;
					if (idx == other.index)
						return err(
								"Der Index " + idx + " ist bereits vergeben.");
					if (isDefault && other.isDefault)
						return err("Ein anderer Gebäudezustand ist bereits als "
								+ "Voreinstellung ausgewählt.");
				}
				setErrorMessage(null);
				Page.this.setPageComplete(true);
				return true;
			}

			BuildingType getType() {
				int idx = typeCombo.getSelectionIndex();
				String item = typeCombo.getItem(idx);
				return Labels.getBuildingType(item);
			}

			boolean err(String msg) {
				Page.this.setErrorMessage(msg);
				Page.this.setPageComplete(false);
				return false;
			}

		}
	}
}
