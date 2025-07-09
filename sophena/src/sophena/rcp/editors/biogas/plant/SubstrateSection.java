package sophena.rcp.editors.biogas.plant;

import java.util.List;
import java.util.UUID;

import org.eclipse.jface.action.Action;
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

	private SubstrateSection() {
	}

	static SubstrateSection of(BiogasPlantEditor editor) {
		SubstrateSection section = new SubstrateSection();
		section.editor = editor;
		return section;
	}

	private BiogasPlant plant() {
		return editor.plant();
	}

	void create(Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, "Substrate");
		Composite composite = UI.sectionClient(section, tk);
		table = createTable(composite);
		Action add = Actions.create(M.Add,
				Icon.ADD_16.des(), this::onAdd);
		Action remove = Actions.create(M.Remove,
				Icon.DELETE_16.des(), this::onRemove);
		Action edit = Actions.create(M.Edit,
				Icon.EDIT_16.des(), this::onEdit);
		Actions.bind(section, add, edit, remove);
		Actions.bind(table, add, edit, remove);
	}

	private TableViewer createTable(Composite composite) {
		TableViewer table = Tables.createViewer(composite,
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
		SubstrateProfile profile = new SubstrateProfile();
		profile.id = UUID.randomUUID().toString();
		int code = SubstrateWizard.open(profile);
		if (code == Window.OK) {
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
		table.setInput(plant().substrateProfiles);
		editor.setDirty();
	}

	private static class Label extends LabelProvider
			implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object obj, int col) {
			return col == 0 ? Icon.PRODUCT_16.img() : null;
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
			if (profile.monthlyPercentages != null && profile.monthlyPercentages.length > 0)
				return "Monatswerte";
			if (profile.hourlyValues != null && profile.hourlyValues.length > 0)
				return "Stundenwerte";
			return "Nicht definiert";
		}
	}
}
