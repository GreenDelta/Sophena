package sophena.rcp.editors.producers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
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
import sophena.model.ProducerFunction;
import sophena.rcp.Images;
import sophena.rcp.Labels;
import sophena.rcp.M;
import sophena.rcp.Numbers;
import sophena.rcp.editors.ComponentCostSection;
import sophena.rcp.utils.Colors;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Desktop;
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
		ScrolledForm form = UI.formHeader(mform, producer().getName());
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		Composite composite = UI.formSection(body, tk, "Erzeugerinformationen");
		createNameText(tk, composite);
		createDescriptionText(tk, composite);
		createBoilerLink(tk, composite);
		createFunctionCombo(tk, composite);
		createRankText(tk, composite);
		new FuelSection(editor).render(body, tk);
		new ComponentCostSection(editor, () -> producer().getCosts())
				.create(body, tk);
	}

	private void createNameText(FormToolkit tk, Composite composite) {
		Text nt = UI.formText(composite, tk, M.Name);
		Texts.set(nt, producer().getName());
		Texts.on(nt).required().onChanged(() -> {
			producer().setName(nt.getText());
			editor.setDirty();
		});
	}

	private void createDescriptionText(FormToolkit tk, Composite composite) {
		Text dt = UI.formMultiText(composite, tk, M.Description);
		Texts.set(dt, producer().getDescription());
		dt.addModifyListener((e) -> {
			producer().setDescription(dt.getText());
			editor.setDirty();
		});
	}

	private void createBoilerLink(FormToolkit tk, Composite composite) {
		UI.formLabel(composite, tk, "Heizkessel");
		Boiler b = producer().getBoiler();
		if (b == null) {
			UI.formLabel(composite, tk, "kein Kessel definiert");
			return;
		}
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

	private void createFunctionCombo(FormToolkit tk, Composite composite) {
		Combo c = UI.formCombo(composite, tk, "Funktion");
		String[] items = { Labels.get(ProducerFunction.BASE_LOAD),
				Labels.get(ProducerFunction.PEAK_LOAD) };
		c.setItems(items);
		if (producer().getFunction() == ProducerFunction.BASE_LOAD)
			c.select(0);
		else
			c.select(1);
		Controls.onSelect(c, (e) -> {
			int i = c.getSelectionIndex();
			if (i == 0) {
				producer().setFunction(ProducerFunction.BASE_LOAD);
			} else {
				producer().setFunction(ProducerFunction.PEAK_LOAD);
			}
			editor.setDirty();
		});
	}

	private void createRankText(FormToolkit tk, Composite composite) {
		Text t = UI.formText(composite, tk, "Rang");
		Texts.set(t, producer().getRank());
		Texts.on(t).required().integer().onChanged(() -> {
			producer().setRank(Texts.getInt(t));
			editor.setDirty();
		});
	}

}
