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
import sophena.model.Consumer;
import sophena.model.FlueGasCleaningEntry;
import sophena.model.HeatNet;
import sophena.model.HeatNetPipe;
import sophena.model.Producer;
import sophena.model.ProductType;
import sophena.model.Project;
import sophena.model.descriptors.ProducerDescriptor;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.editors.consumers.ConsumerEditor;
import sophena.rcp.editors.heatnets.HeatNetEditor;
import sophena.rcp.editors.producers.ProducerEditor;
import sophena.rcp.utils.UI;
import sophena.utils.Strings;

class OverviewPage extends FormPage {

	private CostEditor editor;

	public OverviewPage(CostEditor editor) {
		super(editor, "sophena.CostOverviewPage", "Investitionen");
		this.editor = editor;
		Collections.sort(project().productEntries, (e1, e2) -> {
			if (e1.product == null || e2.product == null)
				return 0;
			else
				return Strings.compare(e1.product.name, e2.product.name);
		});
	}

	private Project project() {
		return editor.getProject();
	}

	@Override
	protected void createFormContent(IManagedForm mform) {
		ScrolledForm form = UI.formHeader(mform, "Investitionen");
		FormToolkit tk = mform.getToolkit();
		Composite body = UI.formBody(form, tk);
		boilers(ProductType.BIOMASS_BOILER, body, tk);
		boilers(ProductType.FOSSIL_FUEL_BOILER, body, tk);
		boilers(ProductType.COGENERATION_PLANT, body, tk);
		entries(ProductType.BOILER_ACCESSORIES, body, tk);
		buffers(body, tk);
		pipes(body, tk);
		transferStations(body, tk);
		heatRecoveries(body, tk);
		flueGasCleanings(body, tk);
		entries(ProductType.BOILER_HOUSE_TECHNOLOGY, body, tk);
		entries(ProductType.BUILDING, body, tk);
		entries(ProductType.HEATING_NET_TECHNOLOGY, body, tk);
		entries(ProductType.HEATING_NET_CONSTRUCTION, body, tk);
		entries(ProductType.PLANNING, body, tk);
		form.reflow(true);
	}

	private void entries(ProductType t, Composite body, FormToolkit tk) {
		EntrySection s = new EntrySection(editor, t);
		s.create(body, tk);
	}

	private void buffers(Composite body, FormToolkit tk) {
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
		s.onOpen = n -> HeatNetEditor.open(project().toDescriptor());
		s.create(body, tk);
	}

	private void pipes(Composite body, FormToolkit tk) {
		DisplaySection<HeatNetPipe> s = new DisplaySection<>(
				ProductType.PIPE);
		HeatNet net = project().heatNet;
		s.content = () -> {
			if (net == null)
				return Collections.emptyList();
			else
				return net.pipes;
		};
		s.costs = p -> p.costs;
		s.label = p -> p.pipe != null ? p.pipe.name : null;
		s.onOpen = p -> HeatNetEditor.open(project().toDescriptor());
		s.create(body, tk);
	}

	private void transferStations(Composite body, FormToolkit tk) {
		DisplaySection<Consumer> s = new DisplaySection<>(
				ProductType.TRANSFER_STATION);
		s.content = () -> {
			List<Consumer> list = new ArrayList<>();
			for (Consumer c : project().consumers) {
				if (!c.disabled && c.transferStation != null) {
					list.add(c);
				}
			}
			Collections.sort(list, (c1, c2) -> Strings.compare(
					c1.transferStation.name, c2.transferStation.name));
			return list;
		};
		s.costs = c -> c.transferStationCosts;
		s.label = c -> c.transferStation.name;
		s.onOpen = c -> ConsumerEditor.open(project().toDescriptor(),
				c.toDescriptor());
		s.create(body, tk);
	}

	private void heatRecoveries(Composite body, FormToolkit tk) {
		DisplaySection<Producer> s = new DisplaySection<>(
				ProductType.HEAT_RECOVERY);
		s.content = () -> {
			List<Producer> list = new ArrayList<>();
			for (Producer p : project().producers) {
				if (p.heatRecovery != null) {
					list.add(p);
				}
			}
			Collections.sort(list, (p1, p2) -> Strings.compare(
					p1.heatRecovery.name, p2.heatRecovery.name));
			return list;
		};
		s.costs = p -> p.heatRecoveryCosts;
		s.label = p -> p.heatRecovery.name;
		s.onOpen = p -> ProducerEditor.open(project().toDescriptor(),
				p.toDescriptor());
		s.create(body, tk);
	}

	public void flueGasCleanings(Composite body, FormToolkit tk) {
		DisplaySection<FlueGasCleaningEntry> s = new DisplaySection<>(
				ProductType.FLUE_GAS_CLEANING);
		s.content = () -> {
			List<FlueGasCleaningEntry> list = new ArrayList<>();
			for (FlueGasCleaningEntry e : project().flueGasCleaningEntries) {
				if (e.product != null) {
					list.add(e);
				}
			}
			Collections.sort(list, (e1, e2) -> Strings.compare(
					e1.product.name, e2.product.name));
			return list;
		};
		s.costs = e -> e.costs;
		s.label = e -> e.product.name;
		s.create(body, tk);
	}

	private void boilers(ProductType type, Composite body, FormToolkit tk) {
		DisplaySection<Producer> s = new DisplaySection<>(type);
		s.content = () -> getProducers(type);
		s.costs = p -> p.costs;
		s.label = p -> p.boiler == null ? null : p.boiler.name;
		s.onOpen = p -> {
			ProjectDescriptor project = project().toDescriptor();
			ProducerDescriptor producer = p.toDescriptor();
			ProducerEditor.open(project, producer);
		};
		s.create(body, tk);
	}

	private List<Producer> getProducers(ProductType type) {
		List<Producer> list = new ArrayList<>();
		for (Producer p : project().producers) {
			if (p.disabled)
				continue;
			Boiler b = p.boiler;
			if (b != null && b.type == type)
				list.add(p);
		}
		Collections.sort(list,
				(p1, p2) -> Strings.compare(p1.boiler.name, p2.boiler.name));
		return list;
	}
}
