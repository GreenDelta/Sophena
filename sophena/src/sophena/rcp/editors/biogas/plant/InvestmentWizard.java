package sophena.rcp.editors.biogas.plant;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.openlca.commons.Strings;

import sophena.Labels;
import sophena.db.daos.ProductGroupDao;
import sophena.model.ProductCosts;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.model.biogas.BiogasInvestmentEntry;
import sophena.model.biogas.BiogasInvestmentGroup;
import sophena.rcp.app.App;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.rcp.wizards.SimpleWizard;
import sophena.utils.Num;

/// Wizard for creating and editing the investment entries of a biogas plant.
/// The refurbishment field is only available for entries of the
/// `BiogasInvestmentGroup#OLD` group.
class InvestmentWizard extends SimpleWizard {

	private final BiogasInvestmentEntry entry;
	private final BiogasInvestmentGroup group;

	private ProductType[] types;
	private Combo typeCombo;
	private EntityCombo<ProductGroup> groupCombo;

	private Text investmentText;
	private Text durationText;
	private Text repairText;
	private Text maintenanceText;
	private Text operationText;

	static int open(BiogasInvestmentEntry entry, BiogasInvestmentGroup group) {
		if (entry == null || group == null)
			return Window.CANCEL;
		return new InvestmentWizard(entry, group).open();
	}

	private InvestmentWizard(
		BiogasInvestmentEntry entry, BiogasInvestmentGroup group) {
		super(Labels.get(group));
		this.entry = entry;
		this.group = group;
		if (entry.costs == null) {
			entry.costs = new ProductCosts();
		}
	}

	@Override
	protected int[] minimumSize() {
		return new int[] { 420, 450 };
	}

	@Override
	protected boolean onFinish() {
		return entry.productGroup != null;
	}

	@Override
	protected void create(Composite comp) {
		UI.gridLayout(comp, 3);
		createTypeCombo(comp);
		createGroupCombo(comp);
		createNameText(comp);
		createCostFields(comp);
		createSpacer(comp);

		// The listeners are only added after the initial selection, so that
		// the initial setup does not overwrite the values of the entry.
		Controls.onSelect(typeCombo, _ -> {
			int i = typeCombo.getSelectionIndex();
			var type = i < 0 ? null : types[i];
			entry.productGroup = null;
			groupCombo.setInput(groupsOf(type));
		});
		groupCombo.onSelect(pg -> {
			entry.productGroup = pg;
			ProductCosts.copy(pg, entry.costs);
			refresh();
		});
	}

	private void createTypeCombo(Composite comp) {
		types = ProductType.values();
		var items = new String[types.length];
		for (int i = 0; i < types.length; i++) {
			items[i] = Labels.get(types[i]);
		}
		typeCombo = UI.formCombo(comp, "Produkttyp");
		typeCombo.setItems(items);
		typeCombo.select(indexOf(initialType()));
		UI.filler(comp);
	}

	private void createGroupCombo(Composite comp) {
		groupCombo = new EntityCombo<>();
		groupCombo.create("Produktgruppe", comp);
		groupCombo.setInput(groupsOf(initialType()));
		if (entry.productGroup != null) {
			groupCombo.select(entry.productGroup);
		}
		UI.filler(comp);
	}

	private ProductType initialType() {
		if (entry.productGroup != null && entry.productGroup.type != null)
			return entry.productGroup.type;
		return types.length > 0 ? types[0] : null;
	}

	private int indexOf(ProductType type) {
		for (int i = 0; i < types.length; i++) {
			if (types[i] == type)
				return i;
		}
		return 0;
	}

	private List<ProductGroup> groupsOf(ProductType type) {
		var groups = new ArrayList<ProductGroup>();
		if (type != null) {
			groups.addAll(new ProductGroupDao(App.getDb()).getAll(type));
		}
		Sorters.productGroups(groups);
		return groups;
	}

	private void createSpacer(Composite comp) {
		var spacer = UI.filler(comp);
		var data = new GridData();
		data.horizontalSpan = 3;
		data.heightHint = 24;
		spacer.setLayoutData(data);
	}

	private void createNameText(Composite comp) {
		var text = UI.formText(comp, "Bezeichnung");
		Texts.set(text, entry.name);
		Texts.on(text)
			.onChanged(s -> entry.name = s);
		UI.filler(comp);
	}

	private void createCostFields(Composite comp) {
		investmentText = UI.formText(comp, "Investitionskosten");
		Texts.on(investmentText)
			.decimal()
			.init(entry.costs.investment)
			.onChanged(s -> entry.costs.investment = Num.read(s));
		UI.formLabel(comp, "EUR");

		if (group == BiogasInvestmentGroup.OLD) {
			var text = UI.formText(comp, "Generalüberholungsbedarf");
			Texts.set(text, entry.refurbishmentShare);
			Texts.on(text)
				.decimal()
				.onChanged(s -> entry.refurbishmentShare =
					Strings.isBlank(s) ? null : Num.read(s));
			UI.formLabel(comp, "%");
		}

		durationText = UI.formText(comp, "Nutzungsdauer");
		Texts.on(durationText)
			.integer()
			.init(entry.costs.duration)
			.onChanged(s -> entry.costs.duration = Num.readInt(s));
		UI.formLabel(comp, "Jahre");

		repairText = UI.formText(comp, "Instandsetzung");
		Texts.on(repairText)
			.decimal()
			.init(entry.costs.repair)
			.onChanged(s -> entry.costs.repair = Num.read(s));
		UI.formLabel(comp, "%");

		maintenanceText = UI.formText(comp, "Wartung und Inspektion");
		Texts.on(maintenanceText)
			.decimal()
			.init(entry.costs.maintenance)
			.onChanged(s -> entry.costs.maintenance = Num.read(s));
		UI.formLabel(comp, "%");

		operationText = UI.formText(comp, "Aufwand für Bedienen");
		Texts.on(operationText)
			.decimal()
			.init(entry.costs.operation)
			.onChanged(s -> entry.costs.operation = Num.read(s));
		UI.formLabel(comp, "h/a");
	}

	private void refresh() {
		Texts.set(investmentText, entry.costs.investment);
		Texts.set(durationText, entry.costs.duration);
		Texts.set(repairText, entry.costs.repair);
		Texts.set(maintenanceText, entry.costs.maintenance);
		Texts.set(operationText, entry.costs.operation);
	}
}
