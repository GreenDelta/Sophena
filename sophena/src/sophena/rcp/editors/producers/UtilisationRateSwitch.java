package sophena.rcp.editors.producers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.model.Producer;
import sophena.rcp.utils.Colors;
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

	public static void create(ProducerEditor editor, Composite c, FormToolkit tk) {
		if (editor == null || c == null || tk == null)
			return;
		new UtilisationRateSwitch(editor).render(c, tk);
	}

	private void render(Composite parent, FormToolkit tk) {
		UI.formLabel(parent, tk, "Nutzungsgrad");
		Composite comp = tk.createComposite(parent);
		UI.innerGrid(comp, 2);
		Button r1 = tk.createButton(comp, "Automatische Berechnung", SWT.RADIO);
		UI.filler(comp);
		Button r2 = tk.createButton(comp, "Manuelle Eingabe", SWT.RADIO);
		text = tk.createText(comp, "");
		UI.gridData(text, false, false).widthHint = 80;
		initText();
		if (producer().utilisationRate == null) {
			r1.setSelection(true);
		} else {
			r2.setSelection(true);
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
			text.setBackground(Colors.forModifiedDefault());
			Texts.set(text, producer().utilisationRate);
		}
		Texts.on(text).decimal().onChanged(t -> {
			if (producer().utilisationRate == null)
				return;
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
		producer().utilisationRate = 0.8;
		Texts.set(text, 0.8);
		text.setBackground(Colors.forModifiedDefault());
		text.setEditable(true);
	}
}
