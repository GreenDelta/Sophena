package sophena.rcp.editors.costs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import sophena.model.Boiler;
import sophena.model.HeatNet;
import sophena.model.Producer;
import sophena.model.ProductType;
import sophena.rcp.Images;
import sophena.rcp.M;
import sophena.rcp.editors.basedata.boilers.BoilerEditor;
import sophena.rcp.editors.basedata.boilers.CoGenPlantEditor;
import sophena.rcp.editors.basedata.buffers.BufferTankEditor;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.Tables;
import sophena.rcp.utils.UI;

class OverviewPage extends FormPage {

	private CostEditor editor;

	public OverviewPage(CostEditor editor) {
		super(editor, "sophena.CostOverviewPage", "Kostenübersicht");
		this.editor = editor;
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Kostenübersicht");
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		boilerSection(ProductType.BIOMASS_BOILER, body, tk);
		boilerSection(ProductType.FOSSIL_FUEL_BOILER, body, tk);
		boilerSection(ProductType.COGENERATION_PLANT, body, tk);
		createSection("Kesselzubehör", body, tk);
		bufferSection(body, tk);
		createSection("Wärmerückgewinnung", body, tk);
		createSection("Rauchgasreinigung", body, tk);
		createSection("Heizhaus-Technik", body, tk);
		createSection("Gebäude", body, tk);
		createSection("Wärmenetz-Technik", body, tk);
		createSection("Wärmenetz-Bau", body, tk);
		createSection("Planung", body, tk);
		form.reflow(true);
	}

	private void bufferSection(Composite body, FormToolkit tk) {
		DisplaySection<HeatNet> s = new DisplaySection<>(
				ProductType.BUFFER_TANK);
		HeatNet net = editor.getProject().heatNet;
		s.content = () -> {
			if (net == null || net.bufferTank == null)
				return Collections.emptyList();
			else
				return Collections.singletonList(net);
		};
		s.costs = (n) -> n.bufferTankCosts;
		s.label = (n) -> n.bufferTank.name;
		s.onOpen = (n) -> BufferTankEditor.open();
		s.create(body, tk);
	}

	private void boilerSection(ProductType type, Composite body,
			FormToolkit tk) {
		DisplaySection<Producer> s = new DisplaySection<>(type);
		s.content = () -> getProducers(type);
		s.costs = (p) -> p.costs;
		s.label = (p) -> p.boiler == null ? null : p.boiler.name;
		s.onOpen = (p) -> {
			if (type == ProductType.COGENERATION_PLANT)
				CoGenPlantEditor.open();
			else
				BoilerEditor.open();
		};
		s.create(body, tk);
	}

	private List<Producer> getProducers(ProductType type) {
		List<Producer> list = new ArrayList<>();
		for (Producer p : editor.getProject().producers) {
			Boiler b = p.boiler;
			if (b != null && b.type == type)
				list.add(p);
		}
		Collections.sort(list,
				(p1, p2) -> Strings.compare(p1.boiler.name, p2.boiler.name));
		return list;
	}

	private void createSection(String label, Composite body, FormToolkit tk) {
		Section section = UI.section(body, tk, label);
		Composite composite = UI.sectionClient(section, tk);
		TableViewer table = createTable(composite);
		Action add = Actions.create(M.Add, Images.ADD_16.des(), () -> {
		});
		Action remove = Actions.create(M.Remove, Images.DELETE_16.des(),
				() -> {
				});
		Action edit = Actions.create(M.Edit, Images.EDIT_16.des(),
				() -> {
				});
		Actions.bind(section, add, edit, remove);
		Actions.bind(table, add, edit, remove);
	}

	private TableViewer createTable(Composite comp) {
		TableViewer table = Tables.createViewer(comp, "Komponente",
				"Investitionskosten", "Nutzungsdauer", "Instandsetzung",
				"Wartung und Inspektion", "Aufwand für Bedienen");
		Tables.bindColumnWidths(table, 0.2, 0.16, 0.16, 0.16, 0.16, 0.16);
		return table;
	}

}
