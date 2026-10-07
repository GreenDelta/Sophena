package sophena.rcp.editors.biogas.plant.manager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.openlca.commons.Strings;

import sophena.db.Database;
import sophena.model.AnnualCostEntry;
import sophena.model.FuelGroup;
import sophena.model.ProductCosts;
import sophena.model.ProductGroup;
import sophena.model.ProductType;
import sophena.model.biogas.BiogasInvestmentEntry;
import sophena.model.biogas.BiogasInvestmentGroup;
import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.BiogasPlantSettings;
import sophena.model.biogas.ElectricityPriceCurve;
import sophena.model.biogas.Fermenter;
import sophena.model.biogas.RoofType;
import sophena.rcp.M;
import sophena.rcp.app.App;
import sophena.rcp.utils.MsgBox;
import sophena.rcp.utils.Texts;
import sophena.rcp.utils.UI;

public class BiogasPlantWizard extends Wizard {

	private final BiogasPlant plant;
	private Page page;

	public static Optional<BiogasPlant> open() {

		// find the product group
		ProductGroup group = null;
		for (var g : App.getDb().getAll(ProductGroup.class)) {
			if (g.type == ProductType.COGENERATION_PLANT
				&& g.fuelGroup == FuelGroup.BIOGAS) {
				group = g;
				break;
			}
		}
		if (group == null) {
			MsgBox.error("Produktgruppe Biogas-BHKW nicht gefunden",
				"Die Produktgruppe Biogas-BHKW wurde nicht in der Datenbank gefunden");
			return Optional.empty();
		}

		var plant = makeNewPlant(group);

		var wizard = new BiogasPlantWizard(plant);
		wizard.setWindowTitle("Neue Biogasanlage");
		var dialog = new WizardDialog(UI.shell(), wizard);
		dialog.setPageSize(150, 450);

		return dialog.open() == Window.OK
			? Optional.of(wizard.plant)
			: Optional.empty();
	}

	private static @NonNull BiogasPlant makeNewPlant(ProductGroup group) {
		var plant = new BiogasPlant();
		plant.id = UUID.randomUUID().toString();
		plant.name = "Neue Biogasanlage";
		plant.duration = 20;
		plant.productGroup = group;
		plant.minimumRuntime = 2;
		plant.gasStorageFillingLevel = 100;
		plant.gasStorageTemperature = 30;
		plant.gasStorageOverpressure = 5;

		// select a default electricity price curver
		plant.electricityPrices = App.getDb().getAll(ElectricityPriceCurve.class)
			.stream()
			.sorted((i, j) -> Strings.compareNatural(j.name, i.name))
			.findAny()
			.orElse(null);

		// create some default cost entries
		var costs = List.of(
			"Laborkosten",
			"Verwaltungskosten",
			"Verzinsung eingelagertes Material",
			"Gärproduktausbringung");
		for (var label : costs) {
			var entry = new AnnualCostEntry();
			entry.value = 0;
			entry.label = label;
			plant.otherAnnualCosts.add(entry);
		}

		// add some investment templates
		var ivs = new InvestmentBuilder(App.getDb(), plant);
		ivs.chp(ProductType.BUILDING, "BHKW-Gebäude");
		ivs.chp(ProductType.BIOGAS_TECHNOLOGY, "BHKW-Vorwärmer");

		ivs.gas("Gasspeicher");
		ivs.gas("Biogasleitungen");
		ivs.gas("Gastrocknung, -entschwefelung und -reinigung");
		ivs.gas("Kompressor");
		ivs.gas("Gasspeicherfüllstandsmessung");
		ivs.gas("Gasmischer");
		ivs.gas("Sonstiges");

		ivs.grid("Stromnetzanschluss");
		ivs.grid("Trafo");
		ivs.grid("Stromübergabestation");
		ivs.grid("Kommunikationseinrichtung");
		ivs.grid("Sonstiges");

		ivs.old(ProductType.BIOGAS_STRUCTURE, "Fahrsilo", 15);
		ivs.old(ProductType.BIOGAS_STRUCTURE, "Vorgrube", 5);
		ivs.old(ProductType.BIOGAS_STRUCTURE, "Silosickersaftbehälter", 5);
		ivs.old(ProductType.BIOGAS_TECHNOLOGY, "Einbringung", 30);
		ivs.old(ProductType.BIOGAS_STRUCTURE, "Fermenter", 10);
		ivs.old(ProductType.BIOGAS_STRUCTURE, "Gärproduktlager", 10);
		ivs.old(ProductType.BIOGAS_TECHNOLOGY, "Rührtechnik", 30);
		ivs.old(ProductType.BIOGAS_TECHNOLOGY, "Behälterabdeckung", 50);
		ivs.old(ProductType.BUILDING,
			"Erschließung, Außenanlagen, Freilager, Zufahrten, Wege, Zäune", 0);
		ivs.old(ProductType.BIOGAS_STRUCTURE, "Umwallung", 0);
		ivs.old(ProductType.BIOGAS_TECHNOLOGY, "Separation", 0);
		ivs.old(ProductType.BIOGAS_TECHNOLOGY, "Pumptechnik", 20);
		ivs.old(ProductType.BIOGAS_TECHNOLOGY, "Technik Rest wenig komplex", 20);
		ivs.old(ProductType.BIOGAS_TECHNOLOGY, "Technik Rest komplex", 20);

		plant.fermenter = defaultFermenter();
		plant.settings = BiogasPlantSettings.createDefault(App.getDb());
		return plant;
	}

	private static Fermenter defaultFermenter() {
		var f = new Fermenter();
		f.id = UUID.randomUUID().toString();
		f.roofType = RoofType.DOUBLE_MEMBRANE;
		f.targetTemperature = 38.0;
		f.wallOuterRadius = 12.32;
		f.wallStructuralThickness = 0.10;
		f.wallInsulationThickness = 0.10;
		f.wallTotalHeight = 10.0;
		f.wallBuriedFraction = 0.50;
		f.roofFixedLayerThickness = 0.01;
		f.roofInsulationThickness = 0.10;
		f.roofMembraneHeight = 4.0;
		f.floorSlabThickness = 0.20;
		f.floorInsulationThickness = 0.10;
		f.wallShadingFraction = 0.50;
		f.roofShadingFraction = 0.50;
		f.mixerPowerDensity = 16.0;
		f.mixerRuntime = 15.0;
		f.mixerHeatFraction = 1.0;
		return f;
	}

	private BiogasPlantWizard(BiogasPlant plant) {
		super();
		this.plant = plant;
	}

	@Override
	public void addPages() {
		page = new Page();
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		page.update(plant);
		App.getDb().insert(plant);
		return true;
	}

	private class Page extends WizardPage {

		private Text nameText;
		private Text durationText;
		private Text descriptionText;

		private Page() {
			super("BiogasPlantWizardPage", "Neue Biogasanlage", null);
			setMessage(" ");
			setPageComplete(false);
		}

		private void update(BiogasPlant plant) {
			plant.name = nameText.getText();
			plant.duration = Texts.getInt(durationText);
			plant.description = descriptionText.getText();
		}

		@Override
		public void createControl(Composite parent) {
			var comp = UI.formComposite(parent);
			setControl(comp);
			UI.gridLayout(comp, 2);

			nameText = UI.formText(comp, M.Name);
			Texts.set(nameText, plant.name);
			Texts.on(nameText).required().validate(this::validate);

			durationText = UI.formText(comp, "Laufzeit (Jahre)");
			Texts.set(durationText, plant.duration);
			Texts.on(durationText).required().integer().validate(this::validate);

			descriptionText = UI.formMultiText(comp, M.Description);
			UI.gridData(descriptionText, true, false).heightHint = 150;

			validate();
		}

		private void validate() {
			if (Texts.isEmpty(nameText)
				|| !Texts.hasNumber(durationText)
				|| Texts.getInt(durationText) <= 0) {
				setPageComplete(false);
				return;
			}
			setPageComplete(true);
		}

	}

	/// A utility class to add same default investment templates.
	@NullMarked
	private record InvestmentBuilder(
		BiogasPlant plant, List<ProductGroup> groups
	) {

		InvestmentBuilder(Database db, BiogasPlant plant) {
			this(plant, db.getAll(ProductGroup.class));
		}

		void chp(ProductType type, String name) {
			add(type, name, BiogasInvestmentGroup.CHP);
		}

		void gas(String name) {
			add(ProductType.BIOGAS_TECHNOLOGY, name, BiogasInvestmentGroup.GAS);
		}

		void grid(String name) {
			add(ProductType.ELECTRICITY_TRANSFER, name, BiogasInvestmentGroup.GRID);
		}

		void old(ProductType type, String name, double share) {
			var entry = add(type, name, BiogasInvestmentGroup.OLD);
			if (entry != null) {
				entry.refurbishmentShare = share;
			}
		}

		@Nullable
		BiogasInvestmentEntry add(
			ProductType type, String name, BiogasInvestmentGroup group
		) {
			var g = findGroup(type, name);
			if (g == null)
				return null;
			var entry = new BiogasInvestmentEntry();
			entry.id = UUID.randomUUID().toString();
			entry.investmentGroup = group;
			entry.productGroup = g;
			entry.costs = ProductCosts.createFrom(g);
			plant.investments.add(entry);
			return entry;
		}

		@Nullable
		private ProductGroup findGroup(ProductType type, String name) {
			for (var g : groups) {
				if (g.type == type && Strings.equalsIgnoreCase(g.name, name))
					return g;
			}
			return null;
		}

	}
}
