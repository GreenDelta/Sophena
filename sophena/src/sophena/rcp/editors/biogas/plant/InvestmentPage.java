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

import sophena.Labels;
import sophena.model.biogas.BiogasInvestmentEntry;
import sophena.model.biogas.BiogasInvestmentGroup;
import sophena.model.biogas.BiogasPlant;
import sophena.rcp.M;
import sophena.rcp.app.Icon;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Lists;
import sophena.utils.Num;

/// The page for editing the investments of a biogas plant. For each investment
/// group a separate section is created, in the order that is defined in
/// `BiogasInvestmentGroup`.
class InvestmentPage extends FormPage {

	private final BiogasPlantEditor editor;

	InvestmentPage(BiogasPlantEditor editor) {
		super(editor, "InvestmentPage", "Investitionen");
		this.editor = editor;
	}

	@Override
	protected void createFormContent(IManagedForm mForm) {
		var form = UI.formHeader(mForm, "Investitionen - " + editor.plant().name);
		var tk = mForm.getToolkit();
		var body = UI.formBody(form, tk);
		for (var group : BiogasInvestmentGroup.values()) {
			InvestmentSection.of(editor, group).create(body, tk);
		}
		editor.calculate();
		form.reflow(true);
	}

	/// A section that shows and edits the investment entries of a biogas plant
	/// that belong to a specific `BiogasInvestmentGroup`.
	private static class InvestmentSection {

		private final BiogasPlantEditor editor;
		private final BiogasInvestmentGroup group;
		private TableViewer table;

		private InvestmentSection(BiogasPlantEditor editor,
			BiogasInvestmentGroup group) {
			this.editor = editor;
			this.group = group;
		}

		static InvestmentSection of(
			BiogasPlantEditor editor, BiogasInvestmentGroup group) {
			return new InvestmentSection(editor, group);
		}

		private BiogasPlant plant() {
			return editor.plant();
		}

		void create(Composite body, FormToolkit tk) {
			var entries = entries();
			var section = entries.isEmpty()
				? UI.collapsedSection(body, tk, Labels.get(group))
				: UI.section(body, tk, Labels.get(group));
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
			table.setLabelProvider(new InvestmentPage.InvestmentLabel(group));
			return table;
		}

		private String[] columns() {
			if (group == BiogasInvestmentGroup.OLD) {
				return new String[] {
					"Produktgruppe",
					"Bezeichnung",
					"Investitionskosten [EUR]",
					"Generalüberholungsbedarf [%]",
					"Nutzungsdauer [a]",
					"Instandsetzung [%]",
					"Wartung und Inspektion [%]",
					"Aufwand fürs Bedienen [h/a]"
				};
			}
			return new String[] {
				"Produktgruppe",
				"Bezeichnung",
				"Investitionskosten [EUR]",
				"Nutzungsdauer [a]",
				"Instandsetzung [%]",
				"Wartung und Inspektion [%]",
				"Aufwand fürs Bedienen [h/a]"
			};
		}

		private List<BiogasInvestmentEntry> entries() {
			var list = new ArrayList<BiogasInvestmentEntry>();
			for (var entry : plant().investments) {
				if (entry != null && entry.investmentGroup == group) {
					list.add(entry);
				}
			}
			return list;
		}

		private void add() {
			var entry = new BiogasInvestmentEntry();
			entry.id = UUID.randomUUID().toString();
			entry.investmentGroup = group;
			if (InvestmentWizard.open(entry, group) != Window.OK)
				return;
			plant().investments.add(entry);
			refresh();
		}

		private void edit() {
			BiogasInvestmentEntry selected = Viewers.getFirstSelected(table);
			selected = Lists.find(selected, plant().investments);
			if (selected == null)
				return;
			var clone = selected.copy();
			if (InvestmentWizard.open(clone, group) != Window.OK)
				return;
			selected.productGroup = clone.productGroup;
			selected.name = clone.name;
			selected.costs = clone.costs;
			selected.refurbishmentShare = clone.refurbishmentShare;
			refresh();
		}

		private void remove() {
			List<BiogasInvestmentEntry> selected = Viewers.getAllSelected(table);
			selected = Lists.findAll(selected, plant().investments);
			if (selected.isEmpty())
				return;
			plant().investments.removeAll(selected);
			refresh();
		}

		private void refresh() {
			table.setInput(entries());
			editor.setDirty();
			editor.calculate();
		}
	}

	private static class InvestmentLabel extends LabelProvider
		implements ITableLabelProvider {

		private final BiogasInvestmentGroup group;

		InvestmentLabel(BiogasInvestmentGroup group) {
			this.group = group;
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

			if (group == BiogasInvestmentGroup.OLD) {
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
