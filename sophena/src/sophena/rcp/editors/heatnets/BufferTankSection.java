package sophena.rcp.editors.heatnets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import sophena.model.BufferTank;
import sophena.model.ComponentCosts;
import sophena.model.HeatNet;
import sophena.rcp.Images;
import sophena.rcp.SearchDialog;
import sophena.rcp.editors.ComponentCostSection;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class BufferTankSection {

	private HeatNetEditor editor;
	private Text volText;

	private ComponentCostSection costSection;

	BufferTankSection(HeatNetEditor editor) {
		this.editor = editor;
	}

	private HeatNet net() {
		return editor.heatNet;
	}

	void create(Composite body, FormToolkit tk) {
		Composite comp = UI.formSection(body, tk, "Pufferspeicher");
		UI.gridLayout(comp, 3);
		createProductRow(comp, tk);
		createVolText(comp, tk);
		if (net().bufferTankCosts == null)
			net().bufferTankCosts = new ComponentCosts();
		costSection = new ComponentCostSection(() -> net().bufferTankCosts)
				.withEditor(editor)
				.createFields(comp, tk);
	}

	private void createVolText(Composite comp, FormToolkit tk) {
		volText = UI.formText(comp, tk, "Volumen");
		Texts.on(volText).init(net().bufferTankVolume)
				.decimal().required().onChanged((s) -> {
					net().bufferTankVolume = Texts.getDouble(volText);
					editor.setDirty();
				});
		UI.formLabel(comp, tk, "L");
	}

	private void createProductRow(Composite comp, FormToolkit tk) {
		UI.formLabel(comp, tk, "Produkt");
		ImageHyperlink link = new ImageHyperlink(comp, SWT.TOP);
		if (net().bufferTank != null)
			link.setText(net().bufferTank.name);
		else
			link.setText("(kein Pufferspeicher ausgewählt)");
		link.setImage(Images.BUFFER_16.img());
		link.setForeground(Colors.getLinkBlue());
		link.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				selectBufferTank(link);
			}
		});
		UI.formLabel(comp, tk, "");
	}

	private void selectBufferTank(ImageHyperlink link) {
		BufferTank b = SearchDialog.open("Pufferspeicher",
				BufferTank.class);
		if (b == null)
			return;
		net().bufferTankVolume = b.volume;
		net().bufferTank = b;
		Texts.set(volText, b.volume);
		link.setText(b.name);
		link.pack();
		if (b.purchasePrice != null) {
			net().bufferTankCosts.investment = b.purchasePrice;
			costSection.refresh();
		}
	}
}