package sophena.rcp.editors.heatnets;

import java.util.Collections;
import java.util.Objects;
import java.util.UUID;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.model.ProductCosts;
import sophena.model.HeatNet;
import sophena.model.HeatNetPipe;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.Numbers;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;

class PipeSection {

	private HeatNetEditor editor;
	private TableViewer table;

	PipeSection(HeatNetEditor editor) {
		this.editor = editor;
	}

	private HeatNet net() {
		return editor.heatNet;
	}

	void create(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, "Wärmeleitungen");
		Composite composite = UI.sectionClient(section, tk);
		UI.gridLayout(composite, 2);
		table = Tables.createViewer(composite, "Komponente", "Länge",
				"Wärmeverlust", "Investitionskosten", "Nutzungsdauer",
				"Instandsetzung", "Wartung und Inspektion",
				"Aufwand für Bedienen");
		Tables.bindColumnWidths(table, 0.3, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1);
		bindActions(section);
		table.setLabelProvider(new Label());
		Collections.sort(net().pipes, (p1, p2) -> {
			if (p1.pipe == null || p2.pipe == null) {
				return 0;
			}
			return Strings.compare(p1.pipe.name, p2.pipe.name);
		});
		table.setInput(net().pipes);
	}

	private void bindActions(Section section) {
		Action add = Actions.create(M.Add, Images.ADD_16.des(), this::add);
		Action edit = Actions.create(M.Edit, Images.EDIT_16.des(), this::edit);
		Action del = Actions.create(M.Delete, Images.DELETE_16.des(),
				this::del);
		Actions.bind(section, add, edit, del);
		Actions.bind(table, add, edit, del);
		Tables.onDoubleClick(table, (e) -> edit());
	}

	private void add() {
		HeatNetPipe pipe = new HeatNetPipe();
		pipe.id = UUID.randomUUID().toString();
		if (PipeWizard.open(pipe) == Window.OK) {
			net().pipes.add(pipe);
			table.setInput(net().pipes);
			editor.setDirty();
		}
	}

	private void edit() {
		HeatNetPipe pipe = getSelected();
		if (pipe == null)
			return;
		HeatNetPipe clone = pipe.clone();
		if (PipeWizard.open(clone) != Window.OK)
			return;
		pipe.costs = clone.costs;
		pipe.length = clone.length;
		pipe.pipe = clone.pipe;
		table.setInput(net().pipes);
		editor.setDirty();
	}

	private void del() {
		HeatNetPipe pipe = getSelected();
		if (pipe == null)
			return;
		net().pipes.remove(pipe);
		table.setInput(net().pipes);
		editor.setDirty();
	}

	private HeatNetPipe getSelected() {
		HeatNetPipe pipe = Viewers.getFirstSelected(table);
		if (pipe == null)
			return null;
		// get the pipe that is really attached to the heat net (JPA)
		for (HeatNetPipe p : net().pipes) {
			if (Objects.equals(pipe.id, p.id))
				return p;
		}
		return pipe;
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return col == 0 ? Images.PIPE_16.img() : null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof HeatNetPipe))
				return null;
			HeatNetPipe pipe = (HeatNetPipe) obj;
			switch (col) {
			case 0:
				return pipe.pipe != null ? pipe.pipe.name : null;
			case 1:
				return Numbers.toString(pipe.length) + " m";
			case 2:
				return null; // TODO: calculate heat loss
			default:
				return getCostLabel(pipe.costs, col);
			}
		}

		private String getCostLabel(ProductCosts costs, int col) {
			if (costs == null)
				return null;
			switch (col) {
			case 3:
				return Numbers.toString(costs.investment) + " EUR";
			case 4:
				return costs.duration + " Jahr(e)";
			case 5:
				return Numbers.toString(costs.repair) + " %";
			case 6:
				return Numbers.toString(costs.maintenance) + " %";
			case 7:
				return Numbers.toString(costs.operation) + " Stunden/Jahr";
			default:
				return null;
			}
		}
	}
}
