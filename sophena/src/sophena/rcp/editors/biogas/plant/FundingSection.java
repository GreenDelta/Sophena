package sophena.rcp.editors.biogas.plant;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.openlca.commons.Strings;

import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.ElectricityMarketValue;
import sophena.model.biogas.MarketPriceLimit;
import sophena.rcp.app.App;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpLink;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

/// The section with the funding settings of a biogas plant: the fixed feed-in
/// tariff or the market premium model. The fields that belong to a mode are
/// only active when that mode is selected.
class FundingSection {

	private final BiogasPlantEditor editor;

	private Button annualRadio;
	private Button monthlyRadio;
	private Texts.TextBox feedInTariff;
	private Texts.TextBox premiumValue;
	private EntityCombo<ElectricityMarketValue> marketValueCombo;
	private Texts.TextBox eligibleQuarterHours;
	private Combo priceLimitCombo;

	private FundingSection(BiogasPlantEditor editor) {
		this.editor = editor;
	}

	static FundingSection of(BiogasPlantEditor editor) {
		return new FundingSection(editor);
	}

	private BiogasPlant plant() {
		return editor.plant();
	}

	void create(Composite body, FormToolkit tk) {
		var comp = UI.formSection(body, tk, "Förderung");
		UI.gridLayout(comp, 4);

		t(comp, tk, "Jährliche Förderungen", "EUR/a",
			plant().settings.annualFunding)
			.onChanged(s -> plant().settings.annualFunding = Num.read(s));
		HelpLink.create(comp, tk, "Jährliche Förderungen", H.AnnualFundingInfo);

		createMarketingRow(comp, tk);
		createTariffRow(comp, tk);
		createPremiumRow(comp, tk);
		createMarketValueRow(comp, tk);
		createValueTypeRow(comp, tk);
		createMarketerRow(comp, tk);
		createEligibleQuarterHoursRow(comp, tk);
		createPriceLimitRow(comp, tk);

		updateState();
	}

	/// The radio buttons that select the type of the electricity marketing.
	private void createMarketingRow(Composite comp, FormToolkit tk) {
		var settings = plant().settings;
		UI.formLabel(comp, tk, "Art der Stromvermarktung");
		var radioComp = tk.createComposite(comp);
		UI.innerGrid(radioComp, 2);
		UI.gridData(radioComp, true, false).horizontalSpan = 3;

		var fixedRadio = tk.createButton(radioComp, "Festvergütung", SWT.RADIO);
		var premiumRadio = tk.createButton(
			radioComp, "Marktprämienmodell", SWT.RADIO);
		fixedRadio.setSelection(settings.isFixedRemuneration);
		premiumRadio.setSelection(!settings.isFixedRemuneration);

		Controls.onSelect(fixedRadio, _ -> {
			settings.isFixedRemuneration = true;
			updateState();
			editor.setDirty();
		});
		Controls.onSelect(premiumRadio, _ -> {
			settings.isFixedRemuneration = false;
			updateState();
			editor.setDirty();
		});
	}

	private void createTariffRow(Composite comp, FormToolkit tk) {
		feedInTariff = t(comp, tk, "Einspeisevergütung", "ct/kWh",
			plant().settings.feedInTariff)
			.onChanged(s -> plant().settings.feedInTariff = Num.read(s));
		UI.filler(comp, tk);
	}

	private void createPremiumRow(Composite comp, FormToolkit tk) {
		premiumValue = t(comp, tk, "Anzulegender Wert", "ct/kWh",
			plant().settings.marketPremiumValue)
			.onChanged(s -> plant().settings.marketPremiumValue = Num.read(s));
		UI.filler(comp, tk);
	}

	private void createMarketValueRow(Composite comp, FormToolkit tk) {
		var settings = plant().settings;
		marketValueCombo = new EntityCombo<ElectricityMarketValue>()
			.create("Marktwert", comp, tk);
		UI.gridData(marketValueCombo.getControl(), true, false);

		var values = new ArrayList<>(
			App.getDb().getAll(ElectricityMarketValue.class));
		values.sort((a, b) -> Strings.compareIgnoreCase(b.name, a.name));
		marketValueCombo.setInput(values);
		marketValueCombo.select(settings.marketValue);
		marketValueCombo.onSelect(v -> {
			settings.marketValue = v;
			editor.setDirty();
		});

		UI.filler(comp, tk);
		UI.filler(comp, tk);
	}

	/// The radio buttons that select the annual or the monthly market value.
	private void createValueTypeRow(Composite comp, FormToolkit tk) {
		var settings = plant().settings;
		UI.filler(comp, tk);
		var radioComp = tk.createComposite(comp);
		UI.innerGrid(radioComp, 2);
		UI.gridData(radioComp, true, false).horizontalSpan = 3;

		annualRadio = tk.createButton(radioComp, "Jahreswert", SWT.RADIO);
		monthlyRadio = tk.createButton(radioComp, "Monatswert", SWT.RADIO);
		annualRadio.setSelection(settings.useAnnualMarketValue);
		monthlyRadio.setSelection(!settings.useAnnualMarketValue);

		Controls.onSelect(annualRadio, _ -> {
			settings.useAnnualMarketValue = true;
			editor.setDirty();
		});
		Controls.onSelect(monthlyRadio, _ -> {
			settings.useAnnualMarketValue = false;
			editor.setDirty();
		});
	}

	private void createMarketerRow(Composite comp, FormToolkit tk) {
		t(comp, tk, "Direktvermarkteranteil", "%",
			plant().settings.directMarketerShare)
			.onChanged(s -> plant().settings.directMarketerShare = Num.read(s));
		HelpLink.create(comp, tk, "Direktvermarkteranteil",
			H.DirectMarketerShareInfo);
	}

	private void createEligibleQuarterHoursRow(
		Composite comp, FormToolkit tk
	) {
		var settings = plant().settings;
		var text = UI.formText(
			comp, tk, "Förderfähige Betriebsviertelstunden");
		eligibleQuarterHours = Texts.on(text)
			.integer()
			.init(settings.eligibleQuarterHours)
			.onChanged(s -> {
				settings.eligibleQuarterHours = Num.readInt(s);
				editor.setDirty();
			});
		UI.formLabel(comp, tk, "BVh/a");
		HelpLink.create(comp, tk, "Förderfähige Betriebsviertelstunden",
			H.EligibleQuarterHoursInfo);
	}

	private void createPriceLimitRow(Composite comp, FormToolkit tk) {
		var settings = plant().settings;
		priceLimitCombo = UI.formCombo(
			comp, tk, "Keine Marktprämie bei Strompreisen");

		var limits = MarketPriceLimit.values();
		var items = new String[limits.length];
		for (int i = 0; i < limits.length; i++) {
			items[i] = limits[i].label();
		}
		priceLimitCombo.setItems(items);

		// plants that were created before this option exists have no limit
		var selected = settings.marketPriceLimit == null
			? MarketPriceLimit.NONE
			: settings.marketPriceLimit;
		settings.marketPriceLimit = selected;
		priceLimitCombo.select(selected.ordinal());

		Controls.onSelect(priceLimitCombo, _ -> {
			int idx = priceLimitCombo.getSelectionIndex();
			if (idx >= 0 && idx < limits.length) {
				settings.marketPriceLimit = limits[idx];
				editor.setDirty();
			}
		});

		UI.filler(comp, tk);
		UI.filler(comp, tk);
	}

	/// Enables the fields that belong to the selected type of the electricity
	/// marketing and disables the other ones.
	private void updateState() {
		boolean fixed = plant().settings.isFixedRemuneration;
		setEnabled(feedInTariff, fixed);
		setEnabled(premiumValue, !fixed);
		setEnabled(marketValueCombo, !fixed);
		setEnabled(annualRadio, !fixed);
		setEnabled(monthlyRadio, !fixed);
		setEnabled(eligibleQuarterHours, !fixed);
		setEnabled(priceLimitCombo, !fixed);
	}

	private void setEnabled(Texts.TextBox box, boolean enabled) {
		if (box == null)
			return;
		if (enabled) {
			box.enable();
		} else {
			box.disable();
		}
	}

	private void setEnabled(EntityCombo<?> combo, boolean enabled) {
		if (combo == null || combo.getControl() == null)
			return;
		combo.getControl().setEnabled(enabled);
	}

	private void setEnabled(Combo combo, boolean enabled) {
		if (combo != null && !combo.isDisposed()) {
			combo.setEnabled(enabled);
		}
	}

	private void setEnabled(Button button, boolean enabled) {
		if (button != null && !button.isDisposed()) {
			button.setEnabled(enabled);
		}
	}

	private Texts.TextBox t(
		Composite comp, FormToolkit tk, String label, String unit, double initial
	) {
		Text text = UI.formText(comp, tk, label);
		UI.formLabel(comp, tk, unit);
		return Texts.on(text)
			.decimal()
			.init(initial)
			.onChanged(_ -> editor.setDirty());
	}
}
