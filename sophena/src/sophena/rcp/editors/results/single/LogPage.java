package sophena.rcp.editors.results.single;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.calc.ProjectResult;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.UI;

public class LogPage extends FormPage {

	private ProjectResult result;

	public LogPage(ResultEditor editor) {
		super(editor, "sophena.LogPage", "Berechnungsdetails");
		this.result = editor.result;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Berechnungsdetails");
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		Text text = tk.createText(body, null, SWT.BORDER | SWT.V_SCROLL
				| SWT.WRAP | SWT.MULTI);
		UI.gridData(text, true, true);
		text.setFont(JFaceResources.getTextFont());
		text.setText(result.calcLog.toString());
		addSimpleSearch(text);
	}

	private void addSimpleSearch(Text text) {
		text.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if ((e.stateMask & SWT.CTRL) != SWT.CTRL)
					return;
				if (e.keyCode != 'f' && e.keyCode != 'F')
					return;
				InputDialog input = new InputDialog(UI.shell(),
						"Suche", "", "", null);
				if (input.open() != Window.OK)
					return;
				String value = input.getValue().trim().toLowerCase();
				String rawText = text.getText().toLowerCase();
				int idx = rawText.indexOf(value);
				if (idx < 0) {
					MsgBox.info("Text wurde nicht gefunden");
					return;
				}
				text.setSelection(idx, idx + value.length());
			}
		});
	}

}
