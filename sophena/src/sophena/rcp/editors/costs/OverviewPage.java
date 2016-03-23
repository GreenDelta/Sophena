package sophena.rcp.editors.costs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import sophena.model.Boiler;
import sophena.model.HeatNet;
import sophena.model.HeatNetPipe;
import sophena.model.Producer;
import sophena.model.ProductType;
import sophena.model.descriptors.ProducerDescriptor;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.editors.heatnets.HeatNetEditor;
import sophena.rcp.editors.producers.ProducerEditor;
import sophena.rcp.utils.Strings;
import sophena.rcp.utils.UI;

class OverviewPage extends FormPage {

	private CostEditor editor;

	public OverviewPage(CostEditor editor) {
		super(editor, "sophena.CostOverviewPage", "Investitionen");
		this.editor = editor;
		Collections.sort(editor.getProject().productEntries, (e1, e2) -> {
			if (e1.product == null || e2.product == null)
				return 0;
			else
				return Strings.compare(e1.product.name, e2.product.name);
		});
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Investitionen");
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		boilerSection(ProductType.BIOMASS_BOILER, body, tk);
		boilerSection(ProductType.FOSSIL_FUEL_BOILER, body, tk);
		boilerSection(ProductType.COGENERATION_PLANT, body, tk);
		entries(ProductType.BOILER_ACCESSORIES, body, tk);
		bufferSection(body, tk);
		pipeSection(body, tk);
		entries(ProductType.HEAT_RECOVERY, body, tk);
		entries(ProductType.FLUE_GAS_CLEANING, body, tk);
		entries(ProductType.BOILER_HOUSE_TECHNOLOGY, body, tk);
		entries(ProductType.BUILDING, body, tk);
		entries(ProductType.HEATING_NET_CONSTRUCTION, body, tk);
		entries(ProductType.PLANNING, body, tk);
		form.reflow(true);
	}

	private void entries(ProductType t, Composite body, FormToolkit tk) {
		EntrySection s = new EntrySection(editor, t);
		s.create(body, tk);
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
		s.costs = n -> n.bufferTankCosts;
		s.label = n -> n.bufferTank.name;
		s.onOpen = n -> HeatNetEditor.open(editor.getProject().toDescriptor());
		s.create(body, tk);
	}

	private void pipeSection(Composite body, FormToolkit tk) {
		DisplaySection<HeatNetPipe> s = new DisplaySection<>(
				ProductType.HEATING_NET_TECHNOLOGY);
		HeatNet net = editor.getProject().heatNet;
		s.content = () -> {
			if (net == null)
				return Collections.emptyList();
			else
				return net.pipes;
		};
		s.costs = p -> p.costs;
		s.label = p -> p.pipe != null ? p.pipe.name : null;
		s.onOpen = p -> HeatNetEditor.open(editor.getProject().toDescriptor());
		s.create(body, tk);
	}

	private void boilerSection(ProductType type, Composite body,
			FormToolkit tk) {
		DisplaySection<Producer> s = new DisplaySection<>(type);
		s.content = () -> getProducers(type);
		s.costs = p -> p.costs;
		s.label = p -> p.boiler == null ? null : p.boiler.name;
		s.onOpen = p -> {
			ProjectDescriptor project = editor.getProject().toDescriptor();
			ProducerDescriptor producer = p.toDescriptor();
			ProducerEditor.open(project, producer);
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

}
