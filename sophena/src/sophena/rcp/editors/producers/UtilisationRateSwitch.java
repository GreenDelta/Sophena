package sophena.rcp.editors.producers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.math.energetic.Producers;
import sophena.model.Producer;
import sophena.rcp.colors.Colors;
import sophena.rcp.help.H;
import sophena.rcp.help.HelpLink;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

class UtilisationRateSwitch {

	private ProducerEditor editor;
	private Text text;

	private UtilisationRateSwitch(ProducerEditor editor) {
		this.editor = editor;
	}

	private Producer producer() {
		return editor.getProducer();
	}
	
	/**
	 * Create a switch for manuel input of the utilisation rate if appropiate
	 * for the producer.
	 */
	public static void checkCreate(ProducerEditor editor, Composite c,
			FormToolkit tk) {
		if (editor == null || c == null || tk == null)
			return;
		Producer p = editor.getProducer();
		if (p == null)
			return;
		if (p.boiler != null && p.boiler.isCoGenPlant)
			return;
		if(p.heatPump != null)
			return;
		new UtilisationRateSwitch(editor).render(c, tk);
	}

	private void render(Composite parent, FormToolkit tk) {
		String title = "Nutzungsgrad";
		UI.formLabel(parent, tk, title);
		Composite comp = tk.createComposite(parent);
		UI.innerGrid(comp, 3);
		Button r1 = tk.createButton(comp, "Automatische Berechnung", SWT.RADIO);
		if (producer().hasProfile() || (producer().solarCollector != null)) {
			r1.setEnabled(false);
		}
		UI.filler(comp, tk);
		UI.filler(comp, tk);
		Button r2 = tk.createButton(comp, "Manuelle Eingabe", SWT.RADIO);
		text = tk.createText(comp, "");
		UI.gridData(text, false, false).widthHint = 80;
		initText();
		if (producer().utilisationRate == null) {
			r1.setSelection(true);
		} else {
			r2.setSelection(true);
		}
		if (Producers.isCoGenPlant(producer())) {
			HelpLink.create(comp, tk, title, H.UtilisationRate);
		}else if (producer().solarCollector != null) {
			HelpLink.create(comp, tk, title, H.UtilisationRateSolarInfo);
		} else {
			UI.filler(comp, tk);
		}
		Controls.onSelect(r1, e -> switchToCalculation());
		Controls.onSelect(r2, e -> switchToInput());
	}

	private void initText() {
		if (text == null)
			return;
		if (producer().utilisationRate == null) {
			text.setBackground(Colors.forCalculatedField());
			text.setEditable(false);
		} else {
			if (!producer().hasProfile()) {
				text.setBackground(Colors.forModifiedDefault());
			} else {
				text.setBackground(Colors.forRequiredField());
			}
			Texts.set(text, producer().utilisationRate);
		}
		Texts.on(text).decimal().onChanged(t -> {
			producer().utilisationRate = Num.read(t);
			editor.setDirty();
		});
	}

	private void switchToCalculation() {
		producer().utilisationRate = null;
		text.setText("");
		text.setBackground(Colors.forCalculatedField());
		text.setEditable(false);
		editor.setDirty();
	}

	private void switchToInput() {
		if (producer().hasProfile())
			return;
		producer().utilisationRate = 0.8;
		Texts.set(text, 0.8);
		text.setBackground(Colors.forModifiedDefault());
		text.setEditable(true);
	}
}
