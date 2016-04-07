package sophena.rcp.editors.consumers;

import java.io.File;
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

import sophena.io.LoadProfileWriter;
import sophena.model.Consumer;
import sophena.model.LoadProfile;
import sophena.model.Stats;
import sophena.rcp.Icon;
import sophena.rcp.M;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.rcp.utils.Viewers;
import sophena.utils.Num;

class LoadProfileSection {

	private ConsumerEditor editor;
	private TableViewer table;

	private LoadProfileSection() {
	}

	static LoadProfileSection of(ConsumerEditor editor) {
		LoadProfileSection section = new LoadProfileSection();
		section.editor = editor;
		return section;
	}

	private Consumer consumer() {
		return editor.getConsumer();
	}

	void create(Composite body, FormToolkit toolkit) {
		Section section = UI.section(body, toolkit, M.LoadProfiles);
		Composite composite = UI.sectionClient(section, toolkit);
		table = Tables.createViewer(composite, M.Name, "Wärmebedarf");
		Tables.bindColumnWidths(table, 0.5, 0.5);
		table.setLabelProvider(new LoadProfileLabel());
		table.setInput(consumer().loadProfiles);
		bindActions(section, table);
	}

	private void bindActions(Section section, TableViewer table) {
		Action add = Actions.create(M.Add, Icon.ADD_16.des(),
				() -> addLoadProfile(table));
		Action edit = Actions.create(M.Edit, Icon.EDIT_16.des(),
				() -> editLoadProfile(table));
		Action del = Actions.create(M.Delete, Icon.DELETE_16.des(),
				() -> deleteLoadProfile(table));
		Action export = Actions.create(M.SaveAsFile, Icon.EXPORT_FILE_16.des(),
				() -> exportProfile(table));
		Actions.bind(section, add, edit, del, export);
		Actions.bind(table, add, edit, del, export);
		Tables.onDoubleClick(table, e -> editLoadProfile(table));
	}

	private void addLoadProfile(TableViewer table) {
		LoadProfile profile = new LoadProfile();
		profile.id = UUID.randomUUID().toString();
		profile.name = "Neuer Lastgang";
		profile.dynamicData = new double[Stats.HOURS];
		profile.staticData = new double[Stats.HOURS];
		if (LoadProfileWizard.open(profile) == Window.OK) {
			List<LoadProfile> profiles = consumer().loadProfiles;
			profiles.add(profile);
			table.setInput(profiles);
			editor.calculate();
			editor.setDirty();
		}
	}

	private void editLoadProfile(TableViewer table) {
		LoadProfile profile = Viewers.getFirstSelected(table);
		if (profile == null)
			return;
		if (LoadProfileWizard.open(profile) == Window.OK) {
			List<LoadProfile> profiles = consumer().loadProfiles;
			table.setInput(profiles);
			editor.calculate();
			editor.setDirty();
		}
	}

	private void deleteLoadProfile(TableViewer table) {
		LoadProfile profile = Viewers.getFirstSelected(table);
		if (profile == null)
			return;
		List<LoadProfile> profiles = consumer().loadProfiles;
		profiles.remove(profile);
		table.setInput(profiles);
		editor.calculate();
		editor.setDirty();
	}

	private void exportProfile(TableViewer table) {
		LoadProfile profile = Viewers.getFirstSelected(table);
		if (profile == null)
			return;
		String name = profile.name == null ? "profile" : profile.name;
		name = name.replaceAll("[^a-zA-Z0-9]+", "_");
		File file = FileChooser.saveFile(name + ".csv", "*.csv");
		if (file == null)
			return;
		LoadProfileWriter writer = new LoadProfileWriter();
		writer.write(profile, file);
	}

	private class LoadProfileLabel extends LabelProvider
			implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int col) {
			return col == 0 ? Icon.LOAD_PROFILE_16.img() : null;
		}

		@Override
		public String getColumnText(Object element, int col) {
			if (!(element instanceof LoadProfile))
				return null;
			LoadProfile profile = (LoadProfile) element;
			switch (col) {
			case 0:
				return profile.name;
			case 1:
				return heatDemand(profile);
			default:
				return null;
			}
		}

		private String heatDemand(LoadProfile profile) {
			double[] totals = profile.calculateTotal();
			double demand = Stats.sum(totals);
			return Num.str(demand) + " kWh";
		}
	}
}
