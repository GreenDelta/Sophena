package sophena.rcp.editors.biogas.plant;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.openlca.commons.Strings;

import sophena.Labels;
import sophena.db.daos.ProductGroupDao;
import sophena.model.ProductCosts;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.model.biogas.BiogasInvestmentEntry;
import sophena.rcp.app.App;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Sorters;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.rcp.wizards.SimpleWizard;
import sophena.utils.Num;

/// Wizard for creating and editing the investment entries of a biogas plant.
/// The product type of an entry is defined by the section in which it is
/// edited, so only product groups of this type can be selected. For entries in
/// the area of refurbishments the share of the investment that is spent for
/// the refurbishment can be edited.
class InvestmentWizard extends SimpleWizard {

	/// The default share [%] of an investment that is spent for a
	/// refurbishment.
	private static final double DEFAULT_SHARE = 0;

	private final BiogasInvestmentEntry entry;
	private final ProductType type;
	private final boolean refurbishment;

	private EntityCombo<ProductGroup> groupCombo;

	private Text investmentText;
	private Text durationText;
	private Text repairText;
	private Text maintenanceText;
	private Text operationText;

	static int open(
		BiogasInvestmentEntry entry, ProductType type, boolean refurbishment) {
		if (entry == null || type == null)
			return Window.CANCEL;
		return new InvestmentWizard(entry, type, refurbishment).open();
	}

	private InvestmentWizard(
		BiogasInvestmentEntry entry, ProductType type, boolean refurbishment) {
		super(Labels.getPlural(type));
		this.entry = entry;
		this.type = type;
		this.refurbishment = refurbishment;
		if (entry.costs == null) {
			entry.costs = new ProductCosts();
		}
		if (refurbishment && entry.refurbishmentShare == null) {
			entry.refurbishmentShare = DEFAULT_SHARE;
		}
	}

	@Override
	protected int[] minimumSize() {
		return new int[] { 420, 450 };
	}

	@Override
	protected boolean onFinish() {
		if (entry.productGroup == null)
			return false;
		if (refurbishment) {
			double share = entry.refurbishmentShare != null
				? entry.refurbishmentShare
				: DEFAULT_SHARE;
			entry.refurbishmentShare = Math.clamp(share, 0, 100);
		}
		return true;
	}

	@Override
	protected void create(Composite comp) {
		UI.gridLayout(comp, 3);
		createGroupCombo(comp);
		createNameText(comp);
		createCostFields(comp);
		createSpacer(comp);

		groupCombo.onSelect(pg -> {
			entry.productGroup = pg;
			ProductCosts.copy(pg, entry.costs);
			refresh();
		});
	}

	private void createGroupCombo(Composite comp) {
		groupCombo = new EntityCombo<>();
		groupCombo.create("Produktgruppe", comp);
		groupCombo.setInput(groupsOf(type));
		if (entry.productGroup != null) {
			groupCombo.select(entry.productGroup);
		}
		UI.filler(comp);
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

		if (refurbishment) {
			var text = UI.formText(comp, "Generalüberholungsbedarf");
			Texts.set(text, entry.refurbishmentShare);
			Texts.on(text)
				.decimal()
				.onChanged(s -> entry.refurbishmentShare = Strings.isBlank(s)
					? DEFAULT_SHARE
					: Num.read(s));
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
