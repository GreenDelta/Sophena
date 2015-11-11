package sophena.rcp.editors.consumers;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.FuelDao;
import sophena.math.energetic.EfficiencyRate;
import sophena.math.energetic.UtilisationRate;
import sophena.model.Fuel;
import sophena.model.FuelConsumption;
import sophena.model.WoodAmountType;
import sophena.rcp.App;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.Numbers;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class ConsumptionWizard extends Wizard {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Page page;
	private FuelConsumption consumption;
	private int loadHours;

	public static int open(FuelConsumption consumption, int loadHours) {
		if (consumption == null)
			return Window.CANCEL;
		ConsumptionWizard wiz = new ConsumptionWizard();
		wiz.setWindowTitle(M.CollectConsumptionData);
		wiz.consumption = consumption;
		wiz.loadHours = loadHours;
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		dialog.setPageSize(150, 400);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		try {
			page.data.bindToModel();
			return true;
		} catch (Exception e) {
			log.error("failed to set consumption data", e);
			return false;
		}
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		private DataBinding data = new DataBinding();

		private EntityCombo<Fuel> fuelCombo;
		private Text amountText;
		private Combo unitCombo;
		private Text waterText;

		private Button effiCheck;
		private Button utilCheck;
		private Text effiText;
		private Text utilText;

		private Page() {
			super("ConsumptionDataWizardPage", M.CollectConsumptionData, null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			setControl(composite);
			UI.gridLayout(composite, 3);
			createFuelCombo(composite);
			createConsumptionRow(composite);
			createBoilerSection(composite);
			data.bindToUI();
		}

		private void createFuelCombo(Composite composite) {
			fuelCombo = new EntityCombo<>();
			fuelCombo.create(M.Fuel, composite);
			UI.formLabel(composite, "");
			fuelCombo.onSelect((f) -> {
				data.selectFuel(f);
				data.validate();
			});
		}

		private void createConsumptionRow(Composite composite) {
			amountText = UI.formText(composite, "Verbrauch pro Jahr");
			Texts.on(amountText).required().decimal();
			unitCombo = new Combo(composite, SWT.READ_ONLY);
			UI.gridData(unitCombo, false, false).widthHint = 25;
			amountText.addModifyListener((e) -> data.validate());
			waterText = UI.formText(composite, M.WaterContent);
			Texts.on(waterText).decimal();
			UI.formLabel(composite, "%");
			waterText.addModifyListener((e) -> data.validate());
		}

		private void createBoilerSection(Composite composite) {
			UI.formLabel(composite, M.BoilerEfficiency);
			effiCheck = createCheck(composite, M.EfficiencyRate);
			effiCheck.setSelection(true);
			effiText = UI.formText(composite, "");
			Texts.on(effiText).decimal();
			UI.formLabel(composite, "%");
			UI.formLabel(composite, "");
			utilCheck = createCheck(composite, M.UtilisationRate);
			utilText = UI.formText(composite, "");
			Texts.on(utilText).decimal();
			utilText.setEnabled(false);
			UI.formLabel(composite, "%");
			addRateListeners();
		}

		private void addRateListeners() {
			Controls.onSelect(effiCheck, (e) -> {
				utilCheck.setSelection(false);
				effiText.setEnabled(true);
				utilText.setEnabled(false);
			});
			Controls.onSelect(utilCheck, (e) -> {
				effiCheck.setSelection(false);
				effiText.setEnabled(false);
				utilText.setEnabled(true);
			});
			AtomicBoolean edit = new AtomicBoolean(false);
			ModifyListener ml = (e) -> {
				if (edit.get())
					return;
				edit.set(true);
				onRateChanged();
				edit.set(false);
			};
			effiText.addModifyListener(ml);
			utilText.addModifyListener(ml);
		}

		private void onRateChanged() {
			if (effiCheck.getSelection()) {
				double effi = Numbers.read(effiText.getText());
				double ur = UtilisationRate
						.ofSmallBoiler()
						.efficiencyRate(effi)
						.fullLoadHours_h(loadHours)
						.get();
				utilText.setText(Numbers.toString(ur));
				data.validate();
			} else {
				double ur = Numbers.read(utilText.getText());
				double er = EfficiencyRate
						.ofSmallBoiler()
						.utilisationRate(ur)
						.fullLoadHours_h(loadHours)
						.get();
				effiText.setText(Numbers.toString(er));
				data.validate();
			}
		}

		private Button createCheck(Composite composite, String label) {
			Composite inner = new Composite(composite, SWT.NONE);
			UI.innerGrid(inner, 2);
			Button check = new Button(inner, SWT.RADIO);
			check.setText(label);
			UI.formLabel(inner, "").setImage(Images.INFO_16.img());
			UI.formLabel(composite, "");
			return check;
		}

		private class DataBinding {

			void bindToModel() {
				Fuel fuel = fuelCombo.getSelected();
				consumption.fuel = fuel;
				consumption.amount = Texts.getDouble(amountText);
				consumption.utilisationRate = Texts.getDouble(utilText);
				if (fuel.wood) {
					consumption.woodAmountType = getWoodType();
					consumption.waterContent = Texts.getDouble(waterText);
				} else {
					consumption.woodAmountType = null;
					consumption.waterContent = (double) 0;
				}
			}

			public WoodAmountType getWoodType() {
				String unit = unitCombo.getItem(unitCombo.getSelectionIndex());
				for (WoodAmountType type : WoodAmountType.values()) {
					if (Strings.nullOrEqual(unit, type.getUnit()))
						return type;
				}
				return null;
			}

			void bindToUI() {
				initFuels();
				Texts.set(amountText, consumption.amount);
				double ur = consumption.utilisationRate;
				if (ur == 0) {
					ur = UtilisationRate
							.ofSmallBoiler()
							.efficiencyRate(90)
							.get();
				}
				Texts.set(utilText, ur);
				double er = EfficiencyRate
						.ofSmallBoiler()
						.utilisationRate(ur)
						.fullLoadHours_h(loadHours)
						.get();
				Texts.set(effiText, er);
				validate();
			}

			private void initFuels() {
				FuelDao dao = new FuelDao(App.getDb());
				List<Fuel> fuels = dao.getAll();
				Sorters.byName(fuels);
				fuelCombo.setInput(fuels);
				if (fuels.isEmpty())
					return;
				Fuel f;
				if (consumption.fuel != null)
					f = consumption.fuel;
				else
					f = fuels.get(0);
				fuelCombo.select(f);
				selectFuel(f);
			}

			private void selectFuel(Fuel fuel) {
				if (fuel == null)
					return;
				initUnit(fuel);
				if (!fuel.wood) {
					waterText.setEnabled(false);
					waterText.setText("");
					return;
				}
				waterText.setEnabled(true);
				if (!Texts.isEmpty(waterText))
					return;
				double wc = 20;
				if (consumption.waterContent > 0)
					wc = consumption.waterContent;
				waterText.setText(Numbers.toString(wc));
			}

			private void initUnit(Fuel fuel) {
				String[] units = getUnits(fuel);
				unitCombo.setItems(units);
				if (!fuel.wood || consumption.woodAmountType == null) {
					unitCombo.select(0);
					return;
				}
				String unit = consumption.woodAmountType.getUnit();
				for (int i = 0; i < units.length; i++) {
					if (Strings.nullOrEqual(unit, units[i])) {
						unitCombo.select(i);
						break;
					}
				}
			}

			private String[] getUnits(Fuel fuel) {
				if (fuel == null)
					return new String[0];
				if (!fuel.wood)
					return new String[] { fuel.unit };
				else {
					WoodAmountType[] woodTypes = WoodAmountType.values();
					String[] units = new String[woodTypes.length];
					for (int i = 0; i < woodTypes.length; i++)
						units[i] = woodTypes[i].getUnit();
					return units;
				}
			}

			boolean validate() {
				Fuel fuel = fuelCombo.getSelected();
				if (fuel == null)
					return error("Es wurde kein Brennstoff ausgewählt.");
				if (!Texts.hasNumber(amountText))
					return error("Es wurde keine gültige Menge eingetragen.");
				if (fuel.wood && !Texts.hasPercentage(waterText))
					return error(
							"Es wurde kein gültiger Wassergehalt eingetragen.");
				if (!Texts.hasPercentage(utilText))
					return error(
							"Es wurde kein gültiger Wirkungsgrad oder Nutzungsgrad eingetragen");
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
