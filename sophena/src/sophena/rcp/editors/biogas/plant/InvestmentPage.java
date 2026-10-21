package sophena.rcp.editors.biogas.plant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import sophena.Labels;
import sophena.model.ProductArea;
import sophena.model.ProductType;
import sophena.model.biogas.BiogasInvestmentEntry;
import sophena.model.biogas.BiogasPlant;
import sophena.rcp.M;
import sophena.rcp.app.Icon;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Lists;
import sophena.utils.Num;

/// The page for editing the investments of a biogas plant. The page contains
/// two areas: one for new investments and one for refurbishments of existing
/// assets. In each area a section is created for each product type of the
/// biogas product area.
class InvestmentPage extends FormPage {

	private final BiogasPlantEditor editor;
	private final List<InvestmentArea> areas = new ArrayList<>();
	private ScrolledForm form;

	InvestmentPage(BiogasPlantEditor editor) {
		super(editor, "InvestmentPage", "Investitionen");
		this.editor = editor;
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		form = UI.formHeader(mForm, "Investitionen");
		var tk = mForm.getToolkit();
		var body = UI.formBody(form, tk);
		createArea(body, tk, "Neuinvestitionen", false);
		createArea(body, tk, "Zurückliegende Investitionen", true);
		editor.calculate();
		form.reflow(true);
	}

	private void createArea(
		Composite body, FormToolkit tk, String label, boolean refurbishment
	) {
		var section = UI.section(body, tk, label);
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		var area = new InvestmentArea(section);
		for (var type : biogasTypes()) {
			var s = new InvestmentSection(this, editor, type, refurbishment);
			s.create(comp, tk);
			area.sections.add(s);
		}
		area.empty = area.isEmpty();
		section.setExpanded(!area.empty);
		areas.add(area);
	}

	private static List<ProductType> biogasTypes() {
		var types = new ArrayList<ProductType>();
		for (var type : ProductType.values()) {
			if (type.productArea == ProductArea.BIOGAS) {
				types.add(type);
			}
		}
		return types;
	}

	/// Refreshes all sections of this page after the investments of the plant
	/// changed. An entry can move to another section when its refurbishment
	/// share changes.
	private void refreshAll() {
		for (var area : areas) {
			for (var s : area.sections) {
				s.refresh();
			}
			boolean empty = area.isEmpty();
			if (empty != area.empty) {
				area.empty = empty;
				area.section.setExpanded(!empty);
				area.section.layout();
			}
		}
		editor.setDirty();
		editor.calculate();
		if (form != null) {
			form.reflow(true);
		}
	}

	/// An area with the sections of the product types of the biogas product
	/// area.
	private static class InvestmentArea {

		private final Section section;
		private final List<InvestmentSection> sections = new ArrayList<>();
		private boolean empty;

		private InvestmentArea(Section section) {
			this.section = section;
		}

		private boolean isEmpty() {
			for (var s : sections) {
				if (!s.entries.isEmpty())
					return false;
			}
			return true;
		}
	}

	/// A section that shows and edits the investment entries of a biogas plant
	/// that belong to a specific product type.
	private static class InvestmentSection {

		private final InvestmentPage page;
		private final BiogasPlantEditor editor;
		private final ProductType type;
		private final boolean refurbishment;

		private final List<BiogasInvestmentEntry> entries = new ArrayList<>();

		private Section section;
		private TableViewer table;
		private boolean empty;

		private InvestmentSection(
			InvestmentPage page,
			BiogasPlantEditor editor,
			ProductType type,
			boolean refurbishment) {
			this.page = page;
			this.editor = editor;
			this.type = type;
			this.refurbishment = refurbishment;
		}

		private BiogasPlant plant() {
			return editor.plant();
		}

		void create(Composite body, FormToolkit tk) {
			fillEntries();
			empty = entries.isEmpty();
			section = empty
				? UI.collapsedSection(body, tk, Labels.getPlural(type))
				: UI.section(body, tk, Labels.getPlural(type));
			var comp = UI.sectionClient(section, tk);
			UI.gridLayout(comp, 1);
			table = createTable(comp);
			table.setInput(entries);

			var add = Actions.create(M.Add, Icon.ADD_16.des(), this::add);
			var edit = Actions.create(M.Edit, Icon.EDIT_16.des(), this::edit);
			var del = Actions.create(M.Remove, Icon.DELETE_16.des(), this::remove);
			Actions.bind(section, add, edit, del);
			Actions.bind(table, add, edit, del);
			Tables.onDoubleClick(table, _ -> edit());
			Tables.onDeletePressed(table, _ -> remove());
		}

		private TableViewer createTable(Composite comp) {
			var columns = columns();
			var table = Tables.createViewer(comp, columns);
			var widths = new double[columns.length];
			Arrays.fill(widths, 1d / columns.length);
			Tables.bindColumnWidths(table, widths);
			table.setLabelProvider(
				new InvestmentPage.InvestmentLabel(refurbishment));
			return table;
		}

		private String[] columns() {
			if (refurbishment) {
				return new String[] {
					"Produktgruppe",
					"Bezeichnung",
					"Investitionskosten [EUR]",
					"Generalüberholungsbedarf [%]",
					"Nutzungsdauer [a]",
					"Instandsetzung [%]",
					"Wartung und Inspektion [%]",
					"Aufwand für Bedienen [h/a]"
				};
			}
			return new String[] {
				"Produktgruppe",
				"Bezeichnung",
				"Investitionskosten [EUR]",
				"Nutzungsdauer [a]",
				"Instandsetzung [%]",
				"Wartung und Inspektion [%]",
				"Aufwand für Bedienen [h/a]"
			};
		}

		/// Fills the entries of this section from the investments of the plant.
		private void fillEntries() {
			entries.clear();
			for (var entry : plant().investments) {
				if (entry == null || entry.productGroup == null)
					continue;
				if (entry.productGroup.type != type)
					continue;
				if ((entry.refurbishmentShare != null) == refurbishment) {
					entries.add(entry);
				}
			}
		}

		/// Reloads the entries from the plant.
		private void refresh() {
			fillEntries();
			if (table != null) {
				table.setInput(entries);
			}
			boolean nowEmpty = entries.isEmpty();
			if (nowEmpty != empty) {
				empty = nowEmpty;
				section.setExpanded(!nowEmpty);
				section.layout();
			}
		}

		private void add() {
			var entry = new BiogasInvestmentEntry();
			entry.id = UUID.randomUUID().toString();
			entry.refurbishmentShare = refurbishment ? 0d : null;
			if (InvestmentWizard.open(entry, type, refurbishment) != Window.OK)
				return;
			plant().investments.add(entry);
			page.refreshAll();
		}

		private void edit() {
			BiogasInvestmentEntry selected = Viewers.getFirstSelected(table);
			selected = Lists.find(selected, plant().investments);
			if (selected == null)
				return;
			var clone = selected.copy();
			if (InvestmentWizard.open(clone, type, refurbishment) != Window.OK)
				return;
			selected.productGroup = clone.productGroup;
			selected.name = clone.name;
			selected.costs = clone.costs;
			selected.refurbishmentShare = clone.refurbishmentShare;
			page.refreshAll();
		}

		private void remove() {
			List<BiogasInvestmentEntry> selected = Viewers.getAllSelected(table);
			selected = Lists.findAll(selected, plant().investments);
			if (selected.isEmpty())
				return;
			plant().investments.removeAll(selected);
			page.refreshAll();
		}
	}

	private static class InvestmentLabel extends LabelProvider
		implements ITableLabelProvider {

		private final boolean refurbishment;

		InvestmentLabel(boolean refurbishment) {
			this.refurbishment = refurbishment;
		}

		@Override
		public Image getColumnImage(Object obj, int col) {
			return null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof BiogasInvestmentEntry entry))
				return null;
			var costs = entry.costs;

			if (refurbishment) {
				return switch (col) {
					case 0 -> groupOf(entry);
					case 1 -> entry.name;
					case 2 -> costs != null ? Num.str(costs.investment) : null;
					case 3 -> entry.refurbishmentShare != null
						? Num.str(entry.refurbishmentShare)
						: null;
					case 4 -> costs != null ? Num.intStr(costs.duration) : null;
					case 5 -> costs != null ? Num.str(costs.repair) : null;
					case 6 -> costs != null ? Num.str(costs.maintenance) : null;
					case 7 -> costs != null ? Num.str(costs.operation) : null;
					default -> null;
				};
			}

			return switch (col) {
				case 0 -> groupOf(entry);
				case 1 -> entry.name;
				case 2 -> costs != null ? Num.str(costs.investment) : null;
				case 3 -> costs != null ? Num.intStr(costs.duration) : null;
				case 4 -> costs != null ? Num.str(costs.repair) : null;
				case 5 -> costs != null ? Num.str(costs.maintenance) : null;
				case 6 -> costs != null ? Num.str(costs.operation) : null;
				default -> null;
			};
		}

		private String groupOf(BiogasInvestmentEntry entry) {
			return entry.productGroup != null
				? entry.productGroup.name
				: null;
		}
	}
}
