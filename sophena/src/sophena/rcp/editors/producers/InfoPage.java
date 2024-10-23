package sophena.rcp.editors.producers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import sophena.Labels;
import sophena.model.OutdoorTemperatureControlKind;
import sophena.model.Producer;
import sophena.model.ProducerFunction;
import sophena.model.ProductType;
import sophena.model.SolarCollectorOperatingMode;
import sophena.rcp.M;
import sophena.rcp.colors.Colors;
import sophena.rcp.editors.basedata.ProductGroupEditor;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

class InfoPage extends FormPage {

	private final ProducerEditor editor;
	private ProfileSection profileSection;
	private Button radioOutdoorFrom;
	private Button radioOutdoorUntil;
	private Text outdoorTemperature;

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
		UtilisationRateSwitch.checkCreate(editor, comp, tk);
		outdoorTemperatureControl(tk, comp);
		importButton(tk, comp);
		if (producer().hasProfile()) {
			profileSection = ProfileSection
					.of(editor)
					.create(body, tk);
		}
		new FuelSection(editor).render(body, tk);
		if (producer().productGroup != null && producer().productGroup.type != null && producer().productGroup.type == ProductType.SOLAR_THERMAL_PLANT)
			new LocationSpecificationSection(editor).create(body, tk);
		if (producer().productGroup != null && producer().productGroup.type != null && producer().productGroup.type == ProductType.HEAT_PUMP)
			new HeatPumpSection(editor).create(body, tk);
		new CostSection(editor).create(body, tk);
		if (!producer().hasProfile()) {
			new InterruptionSection(editor).create(body, tk);
			if(!(producer().productGroup != null && producer().productGroup.type != null && producer().productGroup.type == ProductType.SOLAR_THERMAL_PLANT))
				new HeatRecoverySection(editor).create(body, tk);
		}
		form.reflow(true);
	}

	private void outdoorTemperatureControl(FormToolkit tk, Composite comp)
	{
		createCheck(tk, comp);
		UI.filler(comp);
		UI.formLabel(comp, tk, M.Use);
		Composite inner = tk.createComposite(comp);
		UI.innerGrid(inner, 4);
		if(producer().outdoorTemperatureControlKind == null)
			producer().outdoorTemperatureControlKind = OutdoorTemperatureControlKind.From;
		OutdoorTemperatureControlKind current = producer().outdoorTemperatureControlKind;
		radioOutdoorFrom = tk.createButton(inner, M.From, SWT.RADIO);
		radioOutdoorFrom.setSelection(current == OutdoorTemperatureControlKind.From);
		Controls.onSelect(radioOutdoorFrom, e -> {
			producer().outdoorTemperatureControlKind = OutdoorTemperatureControlKind.From;
			editor.setDirty();
		});
		UI.filler(inner, tk);
		UI.filler(inner, tk);
		UI.filler(inner, tk);
		radioOutdoorUntil = tk.createButton(inner, M.Until, SWT.RADIO);
		radioOutdoorUntil.setSelection(current == OutdoorTemperatureControlKind.Until);
		Controls.onSelect(radioOutdoorUntil, e -> {
			producer().outdoorTemperatureControlKind = OutdoorTemperatureControlKind.Until;
			editor.setDirty();
		});
		
		outdoorTemperature = UI.formText(inner, tk, "");
		UI.gridData(outdoorTemperature, false, false).widthHint = 80;
		Texts.set(outdoorTemperature, producer().outdoorTemperature);
		Texts.on(outdoorTemperature).init(producer().outdoorTemperature).decimal().required()
				.onChanged(s -> {
					producer().outdoorTemperature = Texts.getDouble(outdoorTemperature);					
				});
		UI.formLabel(inner, tk, "°C");		

		enableControls(producer().isOutdoorTemperatureControl);
	}
	
	private void createCheck(FormToolkit tk, Composite comp) {
		var check = tk.createButton(comp, M.ActivateOutdoorTemperature, SWT.CHECK);
		check.setSelection(producer().isOutdoorTemperatureControl);
		Controls.onSelect(check, (e) -> {
			boolean enabled = check.getSelection();
			enableControls(enabled);
			producer().isOutdoorTemperatureControl = enabled;
			editor.setDirty();
		});
	}
	
	private void enableControls(Boolean enable) {
		radioOutdoorFrom.setEnabled(enable);
		radioOutdoorUntil.setEnabled(enable);
		outdoorTemperature.setEnabled(enable);
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
		var link = tk.createImageHyperlink(comp, SWT.NONE);
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
		Combo c = UI.formCombo(comp, tk, M.BufferTank);
		String[] items = { Labels.get(ProducerFunction.BASE_LOAD), Labels.get(ProducerFunction.PEAK_LOAD), Labels.get(ProducerFunction.MAX_LOAD) };
		c.setItems(items);
		if (producer().function == ProducerFunction.BASE_LOAD)
			c.select(0);
		else if (producer().function == ProducerFunction.PEAK_LOAD)
			c.select(1);		
		else
			c.select(2);
		Controls.onSelect(c, (e) -> {
			int i = c.getSelectionIndex();
			if (i == 0) {
				producer().function = ProducerFunction.BASE_LOAD;
			} else if (i==1) {
				producer().function = ProducerFunction.PEAK_LOAD;
			}
			else
				producer().function = ProducerFunction.MAX_LOAD;
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

	private void importButton(FormToolkit tk, Composite comp) {
		if (!producer().hasProfile())
			return;
		UI.filler(comp);
		Button btn = tk.createButton(comp,
				"Neuen Lastgang importieren", SWT.NONE);
		Controls.onSelect(btn, e -> {
			if (profileSection != null) {
				profileSection.importProfile();
			}
		});
	}

}
