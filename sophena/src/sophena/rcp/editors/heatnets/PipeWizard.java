package sophena.rcp.editors.heatnets;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

import sophena.model.HeatNetPipe;
import sophena.model.Pipe;
import sophena.rcp.Images;
import sophena.rcp.SearchDialog;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.UI;

class PipeWizard extends Wizard {

	private Page page;
	private HeatNetPipe pipe;

	public static int open(HeatNetPipe pipe) {
		if (pipe == null)
			return Window.CANCEL;
		PipeWizard w = new PipeWizard();
		w.setWindowTitle("Wärmeleitung");
		w.pipe = pipe;
		WizardDialog dialog = new WizardDialog(UI.shell(), w);
		dialog.setPageSize(150, 400);
		return dialog.open();
	}

	@Override
	public boolean performFinish() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	private class Page extends WizardPage {

		Page() {
			super("HeatNetPipePage", "Wärmeleitung", null);
		}

		@Override
		public void createControl(Composite parent) {
			Composite comp = new Composite(parent, SWT.NONE);
			setControl(comp);
			UI.gridLayout(comp, 3);
			createProductRow(comp);

		}

		private void createProductRow(Composite comp) {
			UI.formLabel(comp, "Produkt");
			ImageHyperlink link = new ImageHyperlink(comp, SWT.TOP);
			if (pipe.pipe != null)
				link.setText(pipe.pipe.name);
			else
				link.setText("(keine Wärmeleitung ausgewählt)");
			link.setImage(Images.PIPE_16.img());
			link.setForeground(Colors.getLinkBlue());
			link.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				public void linkActivated(HyperlinkEvent e) {
					selectPipe(link);
				}
			});
			UI.formLabel(comp, "");

		}

		protected void selectPipe(ImageHyperlink link) {
			Pipe p = SearchDialog.open("Wärmeleitung", Pipe.class);
			if (p == null)
				return;
			pipe.pipe = p;
			link.setText(p.name);
			link.pack();
		}

	}
}
