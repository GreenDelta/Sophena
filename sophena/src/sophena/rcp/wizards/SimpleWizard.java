package sophena.rcp.wizards;

import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import sophena.rcp.utils.UI;

public abstract class SimpleWizard {

	private final String title;

	public SimpleWizard(String title) {
		this.title = title;
	}

	protected abstract boolean onFinish();

	protected abstract void create(Composite content);

	public int open() {
		SWizard wiz = new SWizard();
		wiz.setWindowTitle(title);
		WizardDialog dialog = new WizardDialog(UI.shell(), wiz);
		return dialog.open();
	}

	private class SWizard extends Wizard {
		private Page page;

		@Override
		public boolean performFinish() {
			return SimpleWizard.this.onFinish();
		}

		@Override
		public void addPages() {
			page = new Page();
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
