package sophena.rcp.editors.producers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.db.daos.BoilerDao;
import sophena.model.Boiler;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.rcp.App;
import sophena.rcp.Labels;
import sophena.rcp.M;
import sophena.rcp.editors.ProductCostSection;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.EntityCombo;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

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
		descriptionText(tk, comp);
		boilerCombo(tk, comp);
		functionCombo(tk, comp);
		rankText(tk, comp);
		new FuelSection(editor).render(body, tk);
		new ProductCostSection(() -> producer().costs).withEditor(editor)
				.createSection(body, tk);
		new HeatRecoverySection(editor).create(body, tk);
	}

	private void nameText(FormToolkit tk, Composite comp) {
		Text nt = UI.formText(comp, tk, M.Name);
		Texts.set(nt, producer().name);
		Texts.on(nt).required().onChanged((s) -> {
			producer().name = nt.getText();
			editor.setDirty();
		});
	}

	private void descriptionText(FormToolkit tk, Composite comp) {
		Text dt = UI.formMultiText(comp, tk, M.Description);
		Texts.set(dt, producer().description);
		dt.addModifyListener((e) -> {
			producer().description = dt.getText();
			editor.setDirty();
		});
	}

	private void boilerCombo(FormToolkit tk, Composite comp) {
		EntityCombo<Boiler> combo = new EntityCombo<>();
		combo.create("Heizkessel", comp, tk);
		combo.setLabelProvider(b -> b.name + " ("
				+ Num.str(b.minPower) + " kW - "
				+ Num.str(b.maxPower) + " kW, \u03B7 = "
				+ Num.str(b.efficiencyRate) + "%)");
		Boiler b = producer().boiler;
		if (b == null)
			return;
		combo.setInput(getPossibleBoilers(b));
		combo.select(b);
		combo.onSelect(boiler -> {
			producer().boiler = boiler;
			editor.setDirty();
		});
	}

	private List<Boiler> getPossibleBoilers(Boiler selection) {
		if (selection == null)
			return Collections.emptyList();
		BoilerDao dao = new BoilerDao(App.getDb());
		List<Boiler> all = dao.getAll();
		List<Boiler> filtered = new ArrayList<>();
		for (Boiler b : all) {
			if (Objects.equals(selection.fuel, b.fuel)
					&& Objects.equals(selection.woodAmountType, b.woodAmountType)) {
				filtered.add(b);
			}
		}
		return filtered;
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
