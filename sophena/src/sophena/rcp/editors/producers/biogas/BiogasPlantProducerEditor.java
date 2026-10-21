package sophena.rcp.editors.producers.biogas;

import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.openlca.commons.Strings;

import sophena.calc.biogas.BiogasPlants;
import sophena.calc.biogas.BiogasResult;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.model.Stats;
import sophena.rcp.app.App;
import sophena.rcp.app.Icon;
import sophena.rcp.colors.Colors;
import sophena.rcp.editors.Editor;
import sophena.rcp.editors.biogas.plant.BiogasPlantEditor;
import sophena.rcp.editors.producers.ProducerEditorInput;
import sophena.rcp.navigation.Navigator;
import sophena.rcp.utils.Controls;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;
import sophena.utils.Num;
import sophena.utils.Producers;

public class BiogasPlantProducerEditor extends Editor {

	private Project project;
	private Producer producer;
	private BiogasResult biogasResult;
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
		updateResult();
		Runnable reload = this::reload;
		App.events().subscribe(producer.biogasPlant, reload);
		onClosed(() -> App.events().unsubscribe(reload));
	}

	private void reload() {
		project = App.getDb().get(Project.class, project.id);
		producer = Producers.findById(project, producer.id);
		if (producer == null)
			return;
		updateResult();
		if (page != null) {
			page.refresh();
		}
	}

	/// Calculates the overall result of the plant and applies the runtime result
	/// to the producer. When the plant cannot be calculated, the producer
	/// profile is reset and no default heat price is available.
	private void updateResult() {
		var res = BiogasResult.calculate(producer.biogasPlant);
		if (res.isError()) {
			biogasResult = null;
			BiogasPlants.syncProducerProfile(project, producer);
		} else {
			biogasResult = res.value();
			BiogasPlants.applyResult(project, producer, biogasResult.runtime());
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
		private ImageHyperlink plantLink;
		private Text thermalPowText;
		private Text electricPowText;
		private ProducerChart producerChart;
		private HeatDemandChart heatDemandChart;
		private Text fermenterHeatText;
		private Text fermenterShareText;
		private Text heatPriceText;
		private boolean updatingHeatPrice;

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
			createHeatDemandSection(body, tk);
			refresh();
			form.reflow(true);
		}

		private void createMetaSection(Composite body, FormToolkit tk) {
			var comp = UI.formSection(body, tk, "Wärmeerzeuger");
			UI.gridLayout(comp, 3);

			var producerNameText = UI.formText(comp, tk, "Name");
			Texts.on(producerNameText)
				.init(producer.name)
				.onChanged(name -> {
					producer.name = name;
					setPartName(name);
					form.setText(name);
					setDirty();
				});
			UI.filler(comp, tk);

			UI.formLabel(comp, tk, "Biogasanlage");
			plantLink = tk.createImageHyperlink(comp, SWT.TOP);
			plantLink.setImage(Icon.BOILER_16.img());
			plantLink.setForeground(Colors.getLinkBlue());
			Controls.onClick(plantLink, _ -> openBiogasPlant());
			UI.filler(comp, tk);

			thermalPowText = readOnlyText(comp, tk, "Thermische Nennleistung");
			UI.formLabel(comp, tk, "kW");
			electricPowText = readOnlyText(comp, tk, "Elektrische Nennleistung");
			UI.formLabel(comp, tk, "kW");

			createHeatPriceRow(comp, tk);
		}

		private void createHeatPriceRow(Composite comp, FormToolkit tk) {
			heatPriceText = UI.formText(comp, tk, "Wärmepreis");
			Texts.on(heatPriceText)
				.decimal(1)
				.init(producer.biogasHeatPrice)
				.onChanged(s -> {
					if (updatingHeatPrice)
						return;
					var n = Num.readNumber(s);
					if (n == null) {
						producer.biogasHeatPrice = null;
					} else {
						double value = n.doubleValue();
						var def = defaultHeatPrice();
						producer.biogasHeatPrice = def != null && Num.equal(value, def)
								? null
								: value;
					}
					setDirty();
					updateHeatPriceText();
				});
			UI.formLabel(comp, tk, "EUR/MWh");
		}

		/// The default heat price of the plant if it is available.
		private Double defaultHeatPrice() {
			return biogasResult != null
				? biogasResult.defaultHeatPrice()
				: null;
		}

		/// Shows the heat price that is used in the project calculation: the value
		/// that was entered by the user or the default price of the plant when no
		/// value is defined.
		private void updateHeatPriceText() {
			if (heatPriceText == null || heatPriceText.isDisposed())
				return;
			if (producer.biogasHeatPrice == null) {
				var def = defaultHeatPrice();
				updatingHeatPrice = true;
				Texts.set(heatPriceText, def != null
					? Num.str(def, 1)
					: "–");
				updatingHeatPrice = false;
			}
			heatPriceText.setBackground(producer.biogasHeatPrice == null
				? Colors.forRequiredField()
				: Colors.forModifiedDefault());
		}

		private void createProfileSection(Composite body, FormToolkit tk) {
			var section = UI.section(body, tk, "Erzeugerlastgang");
			UI.gridData(section, true, false);
			var comp = UI.sectionClient(section, tk);
			UI.gridLayout(comp, 1);
			producerChart = ProducerChart.create(comp);
		}

		private void createHeatDemandSection(Composite body, FormToolkit tk) {
			var section = UI.section(body, tk, "Wärmebedarf des Fermenters");
			UI.gridData(section, true, false);
			var comp = UI.sectionClient(section, tk);
			UI.gridLayout(comp, 1);

			var info = tk.createComposite(comp);
			UI.innerGrid(info, 3);
			UI.gridData(info, true, false);
			fermenterHeatText = readOnlyText(info, tk, "Jahresbedarf");
			UI.formLabel(info, tk, "kWh/a");
			fermenterShareText = readOnlyText(
				info, tk, "Anteil an der erzeugten Wärme");
			UI.formLabel(info, tk, "%");

			heatDemandChart = HeatDemandChart.create(comp);
		}

		private Text readOnlyText(Composite parent, FormToolkit tk, String label) {
			var text = UI.formText(parent, tk, label);
			text.setEditable(false);
			return text;
		}

		private void refresh() {
			if (producer == null
				|| producer.biogasPlant == null
				|| form == null
				|| form.isDisposed())
				return;

			var plant = producer.biogasPlant;

			if (plantLink != null && !plantLink.isDisposed()) {
				var plantName = Strings.isNotBlank(plant.name)
					? plant.name
					: "(keine Biogasanlage)";
				plantLink.setText(plantName);
			}
			Texts.set(thermalPowText, Num.intStr(producer.profileMaxPower));
			Texts.set(electricPowText, Num.intStr(producer.profileMaxPowerElectric));
			producerChart.update(producer.profile);

			double fermenterHeat = heatDemandChart.update(project, plant);
			double generatedHeat = producer.profile != null
				&& producer.profile.maxPower != null
					? Stats.sum(producer.profile.maxPower)
					: 0;
			Texts.set(fermenterHeatText, Num.intStr(fermenterHeat));
			if (generatedHeat > 0) {
				double share = 100 * fermenterHeat / generatedHeat;
				Texts.set(fermenterShareText, Num.str(share, 1));
			} else {
				Texts.set(fermenterShareText, "–");
			}
			updateHeatPriceText();

			form.reflow(true);
		}

		private void openBiogasPlant() {
			if (producer == null || producer.biogasPlant == null)
				return;
			BiogasPlantEditor.open(producer.biogasPlant);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			project = App.getDb().get(Project.class, project.id);
			var syncedProducer = Producers.findById(project, producer.id);
			if (syncedProducer == null)
				return;
			syncedProducer.name = producer.name;
			syncedProducer.biogasHeatPrice = producer.biogasHeatPrice;
			project = App.getDb().update(project);
			producer = Producers.findById(project, producer.id);
			setPartName(producer.name);
			Navigator.refresh();
			setSaved();
		} catch (Exception e) {
			log.error(
				"Failed to update producer {} in project {}", producer, project.id, e);
		}
	}

}
