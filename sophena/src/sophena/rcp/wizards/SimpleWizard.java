package sophena.rcp.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.jspecify.annotations.Nullable;

import sophena.rcp.utils.UI;

public abstract class SimpleWizard {

	private final String title;

	public SimpleWizard(String title) {
		this.title = title;
	}

	protected abstract boolean onFinish();

	protected abstract void create(Composite content);

	/// The minimum size of the wizard dialog in dialog units or `null` if no
	/// minimum size should be set. Subclasses can override this method to
	/// request a larger dialog.
	protected int[] minimumSize() {
		return null;
	}

	public int open() {
		SWizard wiz = new SWizard();
		wiz.setWindowTitle(title);
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		var min = minimumSize();
		if (min != null && min.length >= 2) {
			dialog.setMinimumPageSize(min[0], min[1]);
		}
		return dialog.open();
	}

	private class SWizard extends Wizard {

		@Override
		public boolean performFinish() {
			return SimpleWizard.this.onFinish();
		}

		@Override
		public void addPages() {
			var page = new Page();
			addPage(page);
		}
	}

	private class Page extends WizardPage {
		Page() {
			super("SimpleWizardPage", title, null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite comp = new Composite(parent, SWT.NONE);
			setControl(comp);
			SimpleWizard.this.create(comp);
			parent.pack();
		}
	}
}
