package sophena.io.thermos.wizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import sophena.rcp.utils.UI;

public class TransferStationsPage extends WizardPage {

	public TransferStationsPage() {
		super("TransferStationsPage", "Hausübergabestationen", null);
		setMessage("Konfiguration für den Import der Hausübergabestationen.");
	}

	@Override
	public void createControl(Composite parent) {
		Composite root = new Composite(parent, SWT.NONE);
		setControl(root);
		UI.gridLayout(root, 1, 10, 10);

		var label = new org.eclipse.swt.widgets.Label(root, SWT.NONE);
		label.setText("Konfiguration Hausübergabestationen\n\n"
				+ "Hier können später weitere Optionen für den Import\n"
				+ "der Hausübergabestationen konfiguriert werden.");
		UI.gridData(label, true, true);
	}
}
