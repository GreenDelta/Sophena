package sophena.rcp.editors.producers.biogas;

import java.util.Objects;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.calc.biogas.BiogasPlants;
import sophena.model.Producer;
import sophena.model.ProducerProfile;
import sophena.model.Project;
import sophena.rcp.M;
import sophena.rcp.app.App;
import sophena.rcp.charts.ProducerProfileChart;
import sophena.rcp.editors.Editor;
import sophena.rcp.editors.producers.ProducerEditorInput;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;
import sophena.utils.Producers;

public class BiogasPlantProducerEditor extends Editor {

	private Project project;
	private Producer producer;
	private Page page;

	@Override
	public void init(IEditorSite site, IEditorInput editorInput)
			throws PartInitException {
		super.init(site, editorInput);
		var input = ProducerEditorInput.getFrom(editorInput).orElseThrow();
		project = input.project();
		producer = input.producer();
		Objects.requireNonNull(producer.biogasPlant);
		setPartName(producer.name);
		Runnable reload = this::reload;
		App.events().subscribe(producer.biogasPlant, reload);
		onClosed(() -> App.events().unsubscribe(reload));
	}

	private void reload() {
		project = App.getDb().get(Project.class, project.id);
		producer = Producers.findById(project, producer.id);
		if (producer == null) return;
		BiogasPlants.syncProducerProfile(project, producer);
		if (page != null) {
			page.refresh();
		}
	}

	@Override
	protected void addPages() {
		try {
			page = new Page();
			addPage(page);
		} catch (Exception e) {
			log.error("failed to add editor pages", e);
		}
	}

	private class Page extends FormPage {

		private ScrolledForm form;
		private Text plantNameText;
		private Text groupText;
		private Text boilerCountText;
		private Text storageText;
		private Text runtimeText;
		private Text thermalPowerText;
		private Text electricPowerText;
		private ProducerProfileChart chart;

		private Page() {
			super(BiogasPlantProducerEditor.this, "BiogasPlantProducerPage",
					"Biogasanlage");
		}

		@Override
		protected void createFormContent(IManagedForm mForm) {
			form = UI.formHeader(mForm, producer.name);
			var tk = mForm.getToolkit();
			var body = UI.formBody(form, tk);
			createMetaSection(body, tk);
			createProfileSection(body, tk);
			refresh();
			form.reflow(true);
		}

		private void createMetaSection(Composite body, FormToolkit tk) {
			var comp = UI.formSection(body, tk, "Biogasanlage");
			UI.gridLayout(comp, 3);

			plantNameText = readOnlyText(comp, tk, M.Name);
			UI.filler(comp, tk);

			groupText = readOnlyText(comp, tk, "Produktgruppe");
			UI.filler(comp, tk);

			boilerCountText = readOnlyText(comp, tk, "Kessel");
			UI.formLabel(comp, tk, "");

			storageText = readOnlyText(comp, tk, "Gasspeichergröße");
			UI.formLabel(comp, tk, "m3");

			runtimeText = readOnlyText(comp, tk, "Mindestlaufzeit");
			UI.formLabel(comp, tk, "h");

			thermalPowerText = readOnlyText(comp, tk, "Thermische Nennleistung");
			UI.formLabel(comp, tk, "kW");

			electricPowerText = readOnlyText(comp, tk, "Elektrische Nennleistung");
			UI.formLabel(comp, tk, "kW");
		}

		private void createProfileSection(Composite body, FormToolkit tk) {
			var section = UI.section(body, tk, "Erzeugerlastgang");
			UI.gridData(section, true, false);
			var comp = UI.sectionClient(section, tk);
			UI.gridLayout(comp, 1);
			chart = new ProducerProfileChart(comp, 250);
		}

		private Text readOnlyText(Composite parent, FormToolkit tk, String label) {
			var text = UI.formText(parent, tk, label);
			Texts.on(text).readOnly();
			return text;
		}

		private void refresh() {
			if (form == null || form.isDisposed())
				return;
			var plant = producer != null ? producer.biogasPlant : null;
			form.setText(producer != null ? producer.name : "Biogasanlage");
			Texts.set(plantNameText, plant != null ? plant.name : null);
			Texts.set(groupText, plant != null && plant.productGroup != null
					? plant.productGroup.name
					: null);
			Texts.set(boilerCountText, plant != null ? Integer.toString(plant.boilers.size()) : null);
			Texts.set(storageText, plant != null ? Num.str(plant.gasStorageSize) : null);
			Texts.set(runtimeText, plant != null ? Integer.toString(plant.minimumRuntime) : null);
			Texts.set(thermalPowerText, producer != null ? Num.intStr(producer.profileMaxPower) : null);
			Texts.set(electricPowerText,
					producer != null ? Num.intStr(producer.profileMaxPowerElectric) : null);
			ProducerProfile profile = producer != null && producer.profile != null
					? producer.profile
					: ProducerProfile.initEmpty();
			chart.setData(profile);
			form.reflow(true);
		}
	}
}
