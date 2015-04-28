package sophena.rcp.editors.producers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.model.Boiler;
import sophena.model.Producer;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.Numbers;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.Desktop;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

public class InfoPage extends FormPage {

	private ProducerEditor editor;

	public InfoPage(ProducerEditor editor) {
		super(editor, "sophena.ProducerInfoPage", "Wärmeerzeuger");
		this.editor = editor;
	}

	private Producer producer() {
		return editor.getProducer();
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, producer().getName());
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		createInfoSection(body, tk);
	}

	private void createInfoSection(Composite body, FormToolkit tk) {
		Composite composite = UI.formSection(body, tk, "Erzeugerinformationen");
		createNameText(tk, composite);
		createDescriptionText(tk, composite);
		createBoilerLabel(tk, composite);
	}

	private void createNameText(FormToolkit toolkit, Composite composite) {
		Text nt = UI.formText(composite, toolkit, M.Name);
		Texts.set(nt, producer().getName());
		Texts.on(nt).required().onChanged(() -> {
			producer().setName(nt.getText());
			editor.setDirty();
		});
	}

	private void createDescriptionText(FormToolkit toolkit, Composite composite) {
		Text dt = UI.formMultiText(composite, toolkit, M.Description);
		Texts.set(dt, producer().getDescription());
		dt.addModifyListener((e) -> {
			producer().setDescription(dt.getText());
			editor.setDirty();
		});
	}

	private void createBoilerLabel(FormToolkit toolkit, Composite composite) {
		UI.formLabel(composite, toolkit, "Heizkessel");
		Boiler b = producer().getBoiler();
		if (b == null)
			UI.formLabel(composite, toolkit, "kein Kessel definiert");
		else {
			String text = b.getName() + " ("
					+ Numbers.toString(b.getMinPower()) + " kW - "
					+ Numbers.toString(b.getMaxPower()) + " kW, \u03B7 = "
					+ Numbers.toString(b.getEfficiencyRate()) + "%)";
			ImageHyperlink link = new ImageHyperlink(composite, SWT.TOP);
			link.setText(text);
			link.setImage(Images.BOILER_16.img());
			link.setForeground(Colors.getLinkBlue());
			link.addHyperlinkListener(new HyperlinkAdapter() {
				@Override
				public void linkActivated(HyperlinkEvent e) {
					if (b.getUrl() == null)
						return;
					Desktop.browse(b.getUrl());
				}
			});
		}
	}
}
