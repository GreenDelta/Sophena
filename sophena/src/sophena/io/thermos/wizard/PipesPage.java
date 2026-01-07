package sophena.io.thermos.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import sophena.rcp.utils.UI;

public class PipesPage extends WizardPage {

	public PipesPage() {
		super("PipesPage", "Wärmeleitungen", null);
		setMessage("Konfiguration für den Import der Wärmeleitungen.");
	}

	@Override
	public void createControl(Composite parent) {
		Composite root = new Composite(parent, SWT.NONE);
		setControl(root);
		UI.gridLayout(root, 1, 10, 10);

		var label = new org.eclipse.swt.widgets.Label(root, SWT.NONE);
		label.setText("Konfiguration Wärmeleitungen\n\n"
				+ "Hier können später weitere Optionen für den Import\n"
				+ "der Wärmeleitungen konfiguriert werden.");
		UI.gridData(label, true, true);
	}
}
