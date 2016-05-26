package sophena.rcp.editors.producers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import sophena.rcp.utils.UI;

class UtilisationRateSwitch {

	private ProducerEditor editor;
	private Text text;

	private UtilisationRateSwitch(ProducerEditor editor) {
		this.editor = editor;
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
		Button calc = tk.createButton(comp, "Automatische Berechnung", SWT.RADIO);
		UI.filler(comp);
		Button enter = tk.createButton(comp, "Manuelle Eingabe", SWT.RADIO);
		text = tk.createText(comp, "");
	}

}
