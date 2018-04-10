package sophena.rcp.editors.producers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.rcp.Labels;
import sophena.rcp.M;
import sophena.rcp.editors.basedata.ProductGroupEditor;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class InfoPage extends FormPage {

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
		ScrolledForm form = UI.formHeader(mform, producer().name);
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		Composite comp = UI.formSection(body, tk, "Erzeugerinformationen");
		nameText(tk, comp);
		groupLink(tk, comp);
		descriptionText(tk, comp);
		functionCombo(tk, comp);
		rankText(tk, comp);
		UtilisationRateSwitch.create(editor, comp, tk);
		if (producer().hasProfile) {
			ProfileSection.of(editor).create(body, tk);
		}
		new FuelSection(editor).render(body, tk);
		if (!producer().hasProfile) {
			new BoilerSection(editor).create(body, tk);
		}
		new InterruptionSection(editor).create(body, tk);
		if (!producer().hasProfile) {
			new HeatRecoverySection(editor).create(body, tk);
		}
		form.reflow(true);
	}

	private void nameText(FormToolkit tk, Composite comp) {
		Text nt = UI.formText(comp, tk, M.Name);
		Texts.set(nt, producer().name);
		Texts.on(nt).required().onChanged((s) -> {
			producer().name = nt.getText();
			editor.setDirty();
		});
	}

	private void groupLink(FormToolkit tk, Composite comp) {
		UI.formLabel(comp, tk, "Produktgruppe");
		ImageHyperlink link = new ImageHyperlink(comp, SWT.NONE);
		if (producer().productGroup != null) {
			link.setText(producer().productGroup.name);
		} else {
			link.setText("FEHLER: keine Produktgruppe");
		}
		link.setForeground(Colors.getLinkBlue());
		Controls.onClick(link, e -> ProductGroupEditor.open());
	}

	private void descriptionText(FormToolkit tk, Composite comp) {
		Text dt = UI.formMultiText(comp, tk, M.Description);
		Texts.set(dt, producer().description);
		dt.addModifyListener((e) -> {
			producer().description = dt.getText();
			editor.setDirty();
		});
	}

	private void functionCombo(FormToolkit tk, Composite comp) {
		Combo c = UI.formCombo(comp, tk, "Funktion");
		String[] items = { Labels.get(ProducerFunction.BASE_LOAD),
				Labels.get(ProducerFunction.PEAK_LOAD) };
		c.setItems(items);
		if (producer().function == ProducerFunction.BASE_LOAD)
			c.select(0);
		else
			c.select(1);
		Controls.onSelect(c, (e) -> {
			int i = c.getSelectionIndex();
			if (i == 0) {
				producer().function = ProducerFunction.BASE_LOAD;
			} else {
				producer().function = ProducerFunction.PEAK_LOAD;
			}
			editor.setDirty();
		});
	}

	private void rankText(FormToolkit tk, Composite comp) {
		Text t = UI.formText(comp, tk, "Rang");
		Texts.set(t, producer().rank);
		Texts.on(t).required().integer().onChanged((s) -> {
			producer().rank = Texts.getInt(t);
			editor.setDirty();
		});
	}

}
