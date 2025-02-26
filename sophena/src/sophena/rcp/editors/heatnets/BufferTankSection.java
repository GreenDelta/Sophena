package sophena.rcp.editors.heatnets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import sophena.calc.ProjectLoad;
import sophena.db.daos.ProjectDao;
import sophena.model.BufferTank;
import sophena.model.Consumer;
import sophena.model.HeatNet;
import sophena.model.ProductCosts;
import sophena.model.Project;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.SearchDialog;
import sophena.rcp.editors.ProductCostSection;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpLink;
import sophena.rcp.colors.Colors;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.DeleteLink;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class BufferTankSection {

	private final HeatNetEditor editor;
	private Text volText;
	private Text maximumPerformanceText;
	private Text targetChargeText;

	private ProductCostSection costSection;

	BufferTankSection(HeatNetEditor editor) {
		this.editor = editor;
	}

	private HeatNet net() {
		return editor.heatNet;
	}

	private Project project() {
		return editor.project;
	}

	void create(Composite body, FormToolkit tk) {
		Composite comp = UI.formSection(body, tk, "Pufferspeicher");
		UI.gridLayout(comp, 4);
		createProductRow(comp, tk);
		createVolText(comp, tk);
		createTargetChargeLevelText(comp, tk);
		createMaxPerfText(comp, tk);
		createMaxTempText(comp, tk);
		createLowerTempText(comp, tk);
		createLamdaText(comp, tk);
		if (net().bufferTankCosts == null)
			net().bufferTankCosts = new ProductCosts();
		costSection = new ProductCostSection(() -> net().bufferTankCosts)
				.withEditor(editor)
				.createFields(comp, tk);
		targetChargeText.setEnabled(!net().isSeasonalDrivingStyle);
		editor.bus.on("seasonal-driving-changed", this::seasonalDrivingChanged);
	}

	private void createVolText(Composite comp, FormToolkit tk) {
		volText = UI.formText(comp, tk, "Volumen");
		double initial = 0;
		if (net().bufferTank != null) {
			initial = net().bufferTank.volume;
		}
		Texts.on(volText).init(Num.intStr(initial))
				.decimal().calculated();
		UI.formLabel(comp, tk, "L");
		UI.filler(comp, tk);
	}
	
	private void createMaxPerfText(Composite comp, FormToolkit tk) {
		maximumPerformanceText = UI.formText(comp, tk, M.MaxPerformance);
		Texts.on(maximumPerformanceText).init(net().maximumPerformance)
				.decimal().required().onChanged(s -> {
					net().maximumPerformance = Texts.getDouble(maximumPerformanceText);
					editor.setDirty();
				});
		UI.formLabel(comp, tk, "kW");
		HelpLink.create(comp, tk, M.MaxPerformance, H.MaxPerfInfo);
	}

	private void createMaxTempText(Composite comp, FormToolkit tk) {
		Text t = UI.formText(comp, tk, "Maximale Ladetemperatur");
		Texts.on(t).init(net().maxBufferLoadTemperature)
				.decimal().required().onChanged(s -> {
					net().maxBufferLoadTemperature = Texts.getDouble(t);
					editor.setDirty();
				});
		UI.formLabel(comp, tk, "°C");
		UI.filler(comp, tk);
	}

	private void createLowerTempText(Composite comp, FormToolkit tk) {
		/*
		Text t = UI.formText(comp, tk, "Untere Ladetemperatur");
		double initial = net().supplyTemperature;
		Double lowerBuffTemp = net().lowerBufferLoadTemperature;
		if (lowerBuffTemp != null) {
			initial = lowerBuffTemp;
		}
		Texts.on(t).init(initial).decimal().required().onChanged(s -> {
			net().lowerBufferLoadTemperature = Texts.getDouble(t);
			editor.setDirty();
		});
		editor.bus.on("supplyTemperature", () -> {
			if (net().lowerBufferLoadTemperature == null) {
				Texts.set(t, net().supplyTemperature);
			}
		});
		UI.formLabel(comp, tk, "°C");
		UI.filler(comp, tk);
		*/
	}

	private void createLamdaText(Composite comp, FormToolkit tk) {
		Text t = UI.formText(comp, tk, "λ-Wert der Dämmung");
		Texts.on(t).init(net().bufferLambda)
				.decimal().required().onChanged(s -> {
					net().bufferLambda = Texts.getDouble(t);
					editor.setDirty();
				});
		UI.formLabel(comp, tk, "W/m*K");
		HelpLink.create(comp, tk, "λ-Wert der Dämmung", H.BufferLambda);
	}
	
	private void createTargetChargeLevelText(Composite comp, FormToolkit tk) {		
		targetChargeText = UI.formText(comp, tk, "Ziel-Ladestand");
		Texts.on(targetChargeText).decimal().required()
		.init(net().targetChargeLevel)
		.onChanged((s) -> {
			net().targetChargeLevel = Texts.getDouble(targetChargeText);
			editor.setDirty();
		});
		UI.formLabel(comp, tk, "%");
		HelpLink.create(comp, tk, M.TargetChargeLevel, H.TargetChargeLevel);
	}

	private void createProductRow(Composite comp, FormToolkit tk) {
		UI.formLabel(comp, tk, "Produkt");
		Composite inner = tk.createComposite(comp);
		UI.innerGrid(inner, 2);
		var link = tk.createImageHyperlink(inner, SWT.TOP);
		if (net().bufferTank != null) {
			link.setText(net().bufferTank.name);
		} else {
			link.setText("(kein Pufferspeicher ausgewählt)");
		}
		link.setImage(Icon.BUFFER_16.img());
		link.setForeground(Colors.getLinkBlue());
		Controls.onClick(link, e -> selectBufferTank(link));
		DeleteLink.on(inner, () -> {
			if (net().bufferTank == null)
				return;
			net().bufferTank = null;
			link.setText("(kein Pufferspeicher ausgewählt)");
			link.getParent().pack();
			Texts.set(volText, "0");
			ProductCosts.clear(net().bufferTankCosts);
			costSection.refresh();
			editor.setDirty();
		});
		UI.filler(comp, tk);
		UI.filler(comp, tk);
	}
	
	private void seasonalDrivingChanged()
	{
		targetChargeText.setEnabled(!net().isSeasonalDrivingStyle);
	}

	private void selectBufferTank(ImageHyperlink link) {
		BufferTank b = SearchDialog.forBuffers();
		if (b == null)
			return;
		net().bufferTank = b;
		Texts.set(volText, Num.intStr(b.volume));
		link.setText(b.name);
		link.getParent().pack();
		ProductCosts costs = net().bufferTankCosts;
		ProductCosts.copy(b, costs);
		if (b.purchasePrice != null)
			costs.investment = b.purchasePrice;
		costSection.refresh();
		updateMaximumPerformance();
		editor.setDirty();
	}
	
	private void updateMaximumPerformance()
	{
		if (net().bufferTank != null)
		{
			Texts.set(maximumPerformanceText, calculateMaxSimLoad());
			net().maximumPerformance = Texts.getDouble(maximumPerformanceText);
		}
	}
	
	private double calculateMaxSimLoad() {
		HeatNet net = net();
		double max = net.maxLoad == null ? calculateMaxLoad() : net.maxLoad;
		double sim = net.simultaneityFactor * max;
		return Math.ceil(sim);
	}

	private double calculateMaxLoad() {
		try {
			// net load from the net-specification on this page
			double load = ProjectLoad.getMaxNetLoad(project());
			// add consumer loads from the consumers in the database
			// we load them freshly from the database because the may changed
			// in other editors
			ProjectDao dao = new ProjectDao(App.getDb());
			Project p = dao.get(editor.project.id);
			for (Consumer c : p.consumers) {
				if (c.disabled)
					continue;
				load += c.heatingLoad;
			}
			return Math.ceil(load);
		} catch (Exception e) {
			return 0;
		}
	}
	
}
