package sophena.rcp.editors.heatnets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import sophena.model.BufferTank;
import sophena.model.HeatNet;
import sophena.model.ProductCosts;
import sophena.rcp.Icon;
import sophena.rcp.SearchDialog;
import sophena.rcp.editors.ProductCostSection;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpLink;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.DeleteLink;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class BufferTankSection {

	private final HeatNetEditor editor;
	private Text volText;

	private ProductCostSection costSection;

	BufferTankSection(HeatNetEditor editor) {
		this.editor = editor;
	}

	private HeatNet net() {
		return editor.heatNet;
	}

	void create(Composite body, FormToolkit tk) {
		Composite comp = UI.formSection(body, tk, "Pufferspeicher");
		UI.gridLayout(comp, 4);
		createProductRow(comp, tk);
		createVolText(comp, tk);
		createMaxTempText(comp, tk);
		createLowerTempText(comp, tk);
		createLamdaText(comp, tk);
		if (net().bufferTankCosts == null)
			net().bufferTankCosts = new ProductCosts();
		costSection = new ProductCostSection(() -> net().bufferTankCosts)
				.withEditor(editor)
				.createFields(comp, tk);
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
	}

	private void createLamdaText(Composite comp, FormToolkit tk) {
		Text t = UI.formText(comp, tk, "\u03BB-Wert der Dämmung");
		Texts.on(t).init(net().bufferLambda)
				.decimal().required().onChanged(s -> {
					net().bufferLambda = Texts.getDouble(t);
					editor.setDirty();
				});
		UI.formLabel(comp, tk, "W/m*K");
		HelpLink.create(comp, tk, "\u03BB-Wert der Dämmung", H.BufferLambda);
	}

	private void createProductRow(Composite comp, FormToolkit tk) {
		UI.formLabel(comp, tk, "Produkt");
		Composite inner = tk.createComposite(comp);
		UI.innerGrid(inner, 2);
		ImageHyperlink link = new ImageHyperlink(inner, SWT.TOP);
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
		editor.setDirty();
	}
}
