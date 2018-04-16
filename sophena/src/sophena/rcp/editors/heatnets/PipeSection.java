package sophena.rcp.editors.heatnets;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.math.energetic.HeatNets;
import sophena.model.HeatNet;
import sophena.model.HeatNetPipe;
import sophena.model.ProductCosts;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.editors.LoadCurveSection;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Lists;
import sophena.utils.Num;

class PipeSection {

	private HeatNetEditor editor;
	private LoadCurveSection loadCurve;

	private TableViewer table;
	private Text lengthText;
	private Text powerText;

	PipeSection(HeatNetEditor editor) {
		this.editor = editor;
	}

	public void setLoadCurve(LoadCurveSection loadCurve) {
		this.loadCurve = loadCurve;
	}

	private HeatNet net() {
		return editor.heatNet;
	}

	PipeSection create(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, "Wärmeleitungen");
		Composite composite = UI.sectionClient(section, tk);
		UI.gridLayout(composite, 1).verticalSpacing = 10;
		createTable(section, composite, tk);
		createFields(composite, tk);
		editor.bus.on(Arrays.asList(
				"supplyTemperature", "returnTemperature"),
				this::colorTexts);
		colorTexts();
		return this;
	}

	private void createFields(Composite parent, FormToolkit tk) {
		Composite comp = tk.createComposite(parent);
		UI.gridData(comp, true, false);
		GridLayout layout = UI.gridLayout(comp, 4);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		lengthText = UI.formText(comp, tk, "Trassenlänge");
		Texts.on(lengthText).init(net().length).decimal().required()
				.onChanged(s -> {
					net().length = Texts.getDouble(lengthText);
					textsUpdated();
				});
		UI.formLabel(comp, tk, "m");

		Button button = tk.createButton(comp, "Berechnen", SWT.NONE);
		button.setImage(Icon.CALCULATE_16.img());
		powerText = UI.formText(comp, tk, "Verlustleistung");
		Texts.on(powerText).init(net().powerLoss).decimal().required()
				.onChanged(s -> {
					net().powerLoss = Texts.getDouble(powerText);
					textsUpdated();
				});
		UI.formLabel(comp, tk, "W/m");
		UI.formLabel(comp, "");

		Controls.onSelect(button, e -> {
			HeatNet net = net();
			net.length = HeatNets.getTotalSupplyLength(net);
			net.powerLoss = HeatNets.calculatePowerLoss(net);
			Texts.set(lengthText, net.length);
			Texts.set(powerText, net.powerLoss);
			textsUpdated();
		});
	}

	private void textsUpdated() {
		editor.setDirty();
		if (loadCurve != null) {
			loadCurve.setData(NetLoadProfile.get(net()));
		}
		colorTexts();
	}

	private void colorTexts() {
		HeatNet net = net();
		if (net.pipes.isEmpty()) {
			lengthText.setBackground(Colors.forRequiredField());
			powerText.setBackground(Colors.forRequiredField());
			return;
		}
		if (Num.equal(net.powerLoss, HeatNets.calculatePowerLoss(net))) {
			powerText.setBackground(Colors.forRequiredField());
		} else {
			powerText.setBackground(Colors.forModifiedDefault());
		}
		if (Num.equal(net.length, HeatNets.getTotalSupplyLength(net))) {
			lengthText.setBackground(Colors.forRequiredField());
		} else {
			lengthText.setBackground(Colors.forModifiedDefault());
		}
	}

	private void createTable(Section section, Composite parent,
			FormToolkit tk) {
		Composite comp = tk.createComposite(parent);
		UI.gridData(comp, true, false);
		GridLayout layout = UI.gridLayout(comp, 1);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		table = Tables.createViewer(comp, "Name", "Komponente", "Länge",
				"Wärmeverlust", "Investitionskosten", "Nutzungsdauer",
				"Instandsetzung", "Wartung und Inspektion",
				"Aufwand für Bedienen");
		double x = 1d / 9d;
		Tables.bindColumnWidths(table, x, x, x, x, x, x, x, x, x);
		bindActions(section);
		table.setLabelProvider(new Label());
		Collections.sort(net().pipes, (p1, p2) -> {
			int c = Strings.compare(p1.name, p2.name);
			if (c != 0)
				return c;
			if (p1.pipe == null || p2.pipe == null) {
				return 0;
			}
			return Strings.compare(p1.pipe.name, p2.pipe.name);
		});
		table.setInput(net().pipes);
	}

	private void bindActions(Section section) {
		Action add = Actions.create(M.Add, Icon.ADD_16.des(), this::add);
		Action edit = Actions.create(M.Edit, Icon.EDIT_16.des(), this::edit);
		Action del = Actions.create(M.Delete, Icon.DELETE_16.des(),
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
		colorTexts();
	}

	private void edit() {
		HeatNetPipe pipe = Viewers.getFirstSelected(table);
		pipe = Lists.find(pipe, net().pipes); // JPA
		if (pipe == null)
			return;
		HeatNetPipe clone = pipe.clone();
		if (PipeWizard.open(clone) != Window.OK)
			return;
		pipe.name = clone.name;
		pipe.costs = clone.costs;
		pipe.length = clone.length;
		pipe.pipe = clone.pipe;
		pipe.pricePerMeter = clone.pricePerMeter;
		table.setInput(net().pipes);
		editor.setDirty();
		colorTexts();
	}

	private void del() {
		HeatNetPipe pipe = Viewers.getFirstSelected(table);
		pipe = Lists.find(pipe, net().pipes); // JPA
		if (pipe == null)
			return;
		net().pipes.remove(pipe);
		table.setInput(net().pipes);
		editor.setDirty();
		colorTexts();
	}

	private class Label extends LabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return col == 1 ? Icon.PIPE_16.img() : null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof HeatNetPipe))
				return null;
			HeatNetPipe pipe = (HeatNetPipe) obj;
			switch (col) {
			case 0:
				return pipe.name != null ? pipe.name : null;
			case 1:
				return pipe.pipe != null ? pipe.pipe.name : null;
			case 2:
				return Num.str(pipe.length) + " m";
			case 3:
				return Num.str(HeatNets.getPowerLoss(pipe, net()))
						+ " W/m";
			default:
				return getCostLabel(pipe.costs, col);
			}
		}

		private String getCostLabel(ProductCosts costs, int col) {
			if (costs == null)
				return null;
			switch (col) {
			case 4:
				return Num.str(costs.investment) + " EUR";
			case 5:
				return costs.duration + " Jahr(e)";
			case 6:
				return Num.str(costs.repair) + " %";
			case 7:
				return Num.str(costs.maintenance) + " %";
			case 8:
				return Num.str(costs.operation) + " Stunden/Jahr";
			default:
				return null;
			}
		}
	}
}
