package sophena.rcp.editors.basedata.heatpumps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;

import sophena.Labels;
import sophena.io.LoadHeatPumpData;
import sophena.model.HeatPump;
import sophena.model.ProductType;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.editors.basedata.BaseTableLabel;
import sophena.rcp.editors.basedata.ProductWizard;
import sophena.rcp.editors.basedata.ProductWizard.IContent;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpBox;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.Log;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Num;

public class HeatPumpWizard implements IContent {

	private final HeatPump heatPump;

	private List<HeatPumpData> heatPumpDataList = new ArrayList<>();
	private ProductWizard wizard;
	private Text minText;
	private Text ratedPowerText;
	private TableViewer table;

	private HeatPumpWizard(HeatPump heatPump) {
		this.heatPump = heatPump;
	}

	public static int open(HeatPump heatPump) {
		if (heatPump == null || heatPump.type == null)
			return Window.CANCEL;
		HeatPumpWizard content = new HeatPumpWizard(heatPump);
		ProductWizard w = new ProductWizard(heatPump, content);
		content.wizard = w;
		w.setWindowTitle(Labels.get(heatPump.type));
		WizardDialog dialog = new WizardDialog(UI.shell(), w);
		return dialog.open();
	}

	@Override
	public void render(Composite c) {
		createTexts(c);
		Text t = new Text(c.getParent(), 0);
		t.setText(M.OperatingData);
		t.setBackground(c.getBackground());
		Composite comp = new Composite(c.getParent(), SWT.NONE);
		UI.gridData(comp, true, true);
		UI.gridLayout(comp, 1);
		table = Tables.createViewer(comp, getColumns());
		table.setLabelProvider(new Label());

		Tables.bindColumnWidths(table, 0.25, 0.25, 0.3, 0.2);
		var toolBar = bindActions(c.getParent(), table);
		toolBar.moveAbove(comp);

		Button btn = new Button(comp, SWT.NONE);
		btn.setText("Betriebspunkte importieren");
		Controls.onSelect(btn, e -> onSelectFile());

		if(heatPump.targetTemperature != null)
			for(var i = 0; i < heatPump.targetTemperature.length; i++)
				heatPumpDataList.add(new HeatPumpData(heatPump.targetTemperature[i], heatPump.sourceTemperature[i], heatPump.maxPower[i], heatPump.cop[i]));
	}

	private void onSelectFile() {
		var file = FileChooser.open("*.csv", "*.txt");
		if (file == null)
			return;
		try {
			var r = LoadHeatPumpData.readHeatPumpData(file);

			// check error
			if (r.isError()) {
				MsgBox.error(r.message().orElse(
						"Fehler beim Lesen der Datei"));
				return;
			}

			// show warnings
			if (r.isWarning()) {
				MsgBox.warn(r.message().orElse(
						"Die Datei enthält Formatfehler"));
			}

			for(var heatPumpData : r.get())
				heatPumpDataList.add(heatPumpData);
			table.setInput(heatPumpDataList);
			wizard.validate();
		} catch (Exception e) {
			MsgBox.error("Datei konnte nicht gelesen werden",
					e.getMessage());
			Log.error(this, "Failed to read heat pump data file " + file, e);
		}
	}

	private ToolBar bindActions(Composite comp, TableViewer table) {
		Action add = Actions.create(M.Add, Icon.ADD_16.des(),
				() -> add(table));
		Action edit = Actions.create(M.Edit, Icon.EDIT_16.des(),
				() -> edit(table));
		Action del = Actions.create(M.Delete, Icon.DELETE_16.des(),
				() -> delete(table));
		Action info = Actions.create(M.Information,	Icon.INFO_16.des(),
				() -> info(table));
		var control = Actions.bind(comp, add, edit, del, info);
		Actions.bind(table, add, edit, del);
		Tables.onDoubleClick(table, e -> edit(table));
		return control;
	}

	private void add(TableViewer table) {
		HeatPumpData heatPumpData = new HeatPumpData(0, 0, 0, 0);
		if (HeatPumpDataWizard.open(heatPumpData) != Window.OK)
			return;
		heatPumpDataList.add(heatPumpData);
		table.setInput(heatPumpDataList);
		wizard.validate();
	}

	private void edit(TableViewer table) {
		HeatPumpData heatPumpData = Viewers.getFirstSelected(table);
		if(heatPumpData == null)
			return;
		if (HeatPumpDataWizard.open(heatPumpData) != Window.OK)
			return;
		table.setInput(heatPumpDataList);
		wizard.validate();
	}

	private void delete(TableViewer table) {
		HeatPumpData heatPumpData = Viewers.getFirstSelected(table);
		if(heatPumpData == null)
			return;
		if (!MsgBox.ask(M.Delete, "Soll der ausgewählte Eintrag wirklich gelöscht werden?"))
			return;
		heatPumpDataList.remove(heatPumpData);
		table.setInput(heatPumpDataList);
		wizard.validate();
	}

	private void info(TableViewer table)
	{
		HelpBox.show(M.OperatingData, H.OperatingData);
	}

	private String[] getColumns() {
		return new String[] { M.TargetTemperature, M.SourceTemperature, M.MaxPower, M.Cop };
	}

	private void createTexts(Composite c) {
		minText = Texts.on(UI.formText(c, M.MinPower))
			.disableWhen(heatPump.isProtected)
			.decimal()
			.required()
			.validate(wizard::validate)
			.get();
		UI.formLabel(c, "kW");

		ratedPowerText = Texts.on(UI.formText(c, M.RatedPower))
			.disableWhen(heatPump.isProtected)
			.decimal()
			.required()
			.validate(wizard::validate)
			.get();
		UI.formLabel(c, "kW");
	}

	@Override
	public void bindToUI() {
		Texts.set(minText, heatPump.minPower);
		Texts.set(ratedPowerText, heatPump.ratedPower);
		table.setInput(heatPumpDataList);
	}

	@Override
	public void bindToModel() {
		heatPump.minPower = Texts.getDouble(minText);
		heatPump.ratedPower = Texts.getDouble(ratedPowerText);
		var count = heatPumpDataList.size();
		heatPump.targetTemperature = new double[count];
		heatPump.sourceTemperature = new double[count];
		heatPump.maxPower = new double[count];
		heatPump.cop = new double[count];
		for(var i = 0; i < count; i++)
		{
			var heatPumpData = heatPumpDataList.get(i);
			heatPump.targetTemperature[i] = heatPumpData.targetTemperature;
			heatPump.sourceTemperature[i] = heatPumpData.sourceTemperature;
			heatPump.maxPower[i] = heatPumpData.maxPower;
			heatPump.cop[i] = heatPumpData.cop;
		}
	}

	@Override
	public String validate() {
			return valid();
	}

	private String valid() {
		if (!Texts.hasNumber(minText))
			return "Es wurde keine minimale Leistung angegeben";
		if (!Texts.hasNumber(ratedPowerText))
			return "Es wurde keine Nennleistung angegeben";
		if(heatPumpDataList.size() < 2)
			return M.TableError;
		if(!checkEveryValueExistsAlteastTwice(heatPumpDataList))
			return "Bitte geben Sie mindestens 2 Betriebspunkte für jede Zieltemperatur ein.";
		return null;
	}

	private boolean checkEveryValueExistsAlteastTwice(List<HeatPumpData> list)
	{
		Map<Double, Boolean> doubleToBool = new HashMap<>();
		for (var i = 0; i < list.size(); i++)
		{
			var v = list.get(i).targetTemperature;
			if(doubleToBool.containsKey(v))
				doubleToBool.put(v, true);
			else
				doubleToBool.put(v, false);
		}
		if(doubleToBool.values().stream().anyMatch(x -> x == false))
			return false;
		return true;
	}

	@Override
	public String getPageName() {
		return Labels.get(heatPump.type);
	}

	@Override
	public ProductType getProductType() {
		return heatPump.type;
	}

	private class Label extends BaseTableLabel {

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof HeatPumpData))
				return null;
			HeatPumpData heatPumpData = (HeatPumpData) obj;

			switch (col) {
			case 0:
				return s(heatPumpData.targetTemperature, "°C");
			case 1:
				return s(heatPumpData.sourceTemperature, "°C");
			case 2:
				return s(heatPumpData.maxPower, "kW");
			case 3:
				return Num.str(heatPumpData.cop);
			default:
				return null;
			}
		}

		private String s(double val, String unit) {
			return Num.str(val) + " " + unit;
		}
	}

	public static class HeatPumpData
	{
		public double maxPower;
		public double cop;
		public double targetTemperature;
		public double sourceTemperature;

		public HeatPumpData(double targetTemperature, double sourceTemperature, double maxPower, double cop)
		{
			this.targetTemperature = targetTemperature;
			this.sourceTemperature = sourceTemperature;
			this.maxPower = maxPower;
			this.cop = cop;
		}

	}

}
