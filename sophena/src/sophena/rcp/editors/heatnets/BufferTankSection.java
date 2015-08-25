package sophena.rcp.editors.heatnets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import sophena.model.HeatNet;
import sophena.rcp.Images;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class BufferTankSection {

	private HeatNetEditor editor;
	private Text volText;

	BufferTankSection(HeatNetEditor editor) {
		this.editor = editor;
	}

	private HeatNet net() {
		return editor.heatNet;
	}

	void create(Composite body, FormToolkit tk) {
		Composite comp = UI.formSection(body, tk, "Pufferspeicher");
		UI.gridLayout(comp, 3);
		createVolText(comp, tk);
		createProductRow(comp, tk);
	}

	private void createVolText(Composite comp, FormToolkit tk) {
		volText = UI.formText(comp, tk, "Volumen");
		GridData gd = UI.gridData(volText, false, false);
		gd.horizontalAlignment = SWT.FILL;
		gd.widthHint = 250;
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
		link.setText("(kein Pufferspeicher ausgewählt)");
		link.setImage(Images.BUFFER_16.img());
		link.setForeground(Colors.getLinkBlue());
		link.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				// link.setText(link.getText() + "super!");
				link.pack();
			}
		});
		UI.formLabel(comp, tk, "");
	}

}
