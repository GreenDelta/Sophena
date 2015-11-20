package sophena.rcp.editors.basedata;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import sophena.db.daos.RootEntityDao;
import sophena.model.BuildingState;
import sophena.rcp.App;
import sophena.rcp.Images;
import sophena.rcp.Labels;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;
import sophena.utils.Num;

public class BuildingStateEditor extends Editor {

	public static void open() {
		KeyEditorInput input = new KeyEditorInput("data.building.states",
				"Gebäudetypen");
		Editors.open(input, "sophena.BuildingStateEditor");
	}

	@Override
	protected void addPages() {
		try {
			addPage(new Page());
		} catch (Exception e) {
			log.error("failed to add page", e);
		}
	}

	private class Page extends FormPage {

		private RootEntityDao<BuildingState> dao;
		private List<BuildingState> states;

		public Page() {

			super(BuildingStateEditor.this, "BuildingStatePage",
					"Gebäudetypen");
			dao = new RootEntityDao<>(BuildingState.class, App.getDb());
			states = dao.getAll();
			Collections.sort(states, (s1, s2) -> {
				if (s2.type == null || s1.type == null)
					return 0;
				if (s1.type != s2.type)
					return s1.type.ordinal() - s2.type.ordinal();
				return s1.index - s2.index;
			});
		}

		@Override
		protected void createFormContent(IManagedForm managedForm) {
			ScrolledForm form = UI.formHeader(managedForm, "Gebäudetypen");
			FormToolkit toolkit = managedForm.getToolkit();
			Composite body = UI.formBody(form, toolkit);
			createStateSection(body, toolkit);
			form.reflow(true);

		}

		private void createStateSection(Composite parent, FormToolkit toolkit) {
			Section section = UI.section(parent, toolkit, "Gebäudetypen");
			UI.gridData(section, true, true);
			Composite comp = UI.sectionClient(section, toolkit);
			UI.gridLayout(comp, 1);
			TableViewer table = Tables.createViewer(comp, "Gebäudetyp",
					"Gebäudezustand", "Heizgrenztemperatur", "Warmwasseranteil",
					"Volllaststunden", "Voreinstellung");
			table.setLabelProvider(new BuildingStateLabel());
			table.setInput(states);
			double x = 1 / 6d;
			Tables.bindColumnWidths(table, x, x, x, x, x, x);

		}

		private class BuildingStateLabel extends LabelProvider
				implements ITableLabelProvider {

			@Override
			public Image getColumnImage(Object obj, int col) {
				if (!(obj instanceof BuildingState))
					return null;
				BuildingState s = (BuildingState) obj;
				if (col == 0)
					return s.index == 0 ? Images.BUILDING_TYPE_16.img() : null;
				if (col == 5)
					return s.isDefault ? Images.CHECKBOX_CHECKED_16.img()
							: Images.CHECKBOX_UNCHECKED_16.img();
				else
					return null;
			}

			@Override
			public String getColumnText(Object obj, int col) {
				if (!(obj instanceof BuildingState))
					return null;
				BuildingState s = (BuildingState) obj;
				switch (col) {
				case 0:
					return s.index == 0 ? Labels.get(s.type) : null;
				case 1:
					return s.name;
				case 2:
					return Num.str(s.heatingLimit) + " °C";
				case 3:
					return Num.str(s.waterFraction) + " %";
				case 4:
					return Num.intStr(s.loadHours) + " h";
				default:
					return null;
				}
			}
		}
	}
}
