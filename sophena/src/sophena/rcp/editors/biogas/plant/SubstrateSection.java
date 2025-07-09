package sophena.rcp.editors.biogas.plant;

import java.util.List;
import java.util.UUID;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import sophena.model.biogas.BiogasPlant;
import sophena.model.biogas.SubstrateProfile;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Num;

class SubstrateSection {

	private BiogasPlantEditor editor;
	private TableViewer table;
	private MethaneChart chart;

	private SubstrateSection() {
	}

	static SubstrateSection of(BiogasPlantEditor editor) {
		var section = new SubstrateSection();
		section.editor = editor;
		return section;
	}

	private BiogasPlant plant() {
		return editor.plant();
	}

	void create(Composite body, FormToolkit tk) {
		var section = UI.section(body, tk, "Substrate");
		var comp = UI.sectionClient(section, tk);
		UI.gridLayout(comp, 1);
		table = createTable(comp);
		addSubstrateActions(section);

		var chartSection = UI.section(body, tk, "Erzeugtes Methan");
		var chartComp = UI.sectionClient(chartSection, tk);
		UI.gridLayout(chartComp, 1);
		chart = new MethaneChart(chartComp, 200);
		chart.setInput(plant().substrateProfiles);
	}

	private void addSubstrateActions(Section section) {
		var add = Actions.create(M.Add, Icon.ADD_16.des(), this::onAdd);
		var remove = Actions.create(M.Remove, Icon.DELETE_16.des(), this::onRemove);
		var edit = Actions.create(M.Edit, Icon.EDIT_16.des(), this::onEdit);
		var xls = Actions.create("Profilexport", Icon.EXCEL_16.des(), () -> {
			SubstrateProfile p = Viewers.getFirstSelected(table);
			if (p != null) {
				SubstrateProfileIO.write(p);
			}
		});

		Actions.bind(section, add, edit, remove);
		Actions.bind(table, add, edit, xls, remove);
	}

	private TableViewer createTable(Composite composite) {
		var table = Tables.createViewer(composite,
				"Substrat",
				"Menge (t/a)",
				"Kosten (€/t)",
				"Verteilung");
		Tables.bindColumnWidths(table, 0.3, 0.2, 0.2, 0.3);
		table.setLabelProvider(new Label());
		table.setInput(plant().substrateProfiles);
		Tables.onDoubleClick(table, (e) -> onEdit());
		return table;
	}

	private void onAdd() {
		var profile = new SubstrateProfile();
		profile.id = UUID.randomUUID().toString();
		profile.monthlyPercentages = new double[]{
				8.5, 7.7, 8.5, 8.2, 8.5, 8.2, 8.5, 8.5, 8.2, 8.5, 8.2, 8.5};
		if (SubstrateWizard.open(profile) == Window.OK) {
			plant().substrateProfiles.add(profile);
			updateUI();
		}
	}

	private void onRemove() {
		List<SubstrateProfile> list = Viewers.getAllSelected(table);
		if (list == null || list.isEmpty())
			return;
		plant().substrateProfiles.removeAll(list);
		updateUI();
	}

	private void onEdit() {
		SubstrateProfile profile = Viewers.getFirstSelected(table);
		if (profile == null)
			return;
		int code = SubstrateWizard.open(profile);
		if (code == Window.OK) {
			updateUI();
		}
	}

	private void updateUI() {
		var profiles = plant().substrateProfiles;
		table.setInput(profiles);
		chart.setInput(profiles);
		editor.setDirty();
	}

	private static class Label extends LabelProvider
			implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return col == 0
					? Icon.BIOGAS_SUBSTRATE_16.img()
					: null;
		}

		@Override
		public String getColumnText(Object obj, int col) {
			if (!(obj instanceof SubstrateProfile profile))
				return null;
			return switch (col) {
				case 0 -> profile.substrate != null ? profile.substrate.name : null;
				case 1 -> Num.str(profile.annualMass);
				case 2 -> Num.str(profile.substrateCosts);
				case 3 -> getDistributionInfo(profile);
				default -> null;
			};
		}

		private String getDistributionInfo(SubstrateProfile profile) {
			if (profile == null)
				return null;
			if (profile.monthlyPercentages != null)
				return "Monatswerte";
			if (profile.hourlyValues != null)
				return "Stundenwerte";
			return "Nicht definiert";
		}
	}
}
