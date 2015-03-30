package sophena.rcp.navigation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Image;

import sophena.model.Consumer;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.rcp.Images;
import sophena.rcp.M;

public class StructureElement implements NavigationElement {

	public static final int PRODUCTION = 0;
	public static final int DISTRIBUTION = 1;
	public static final int CONSUMPTION = 2;
	public static final int COSTS = 3;
	public static final int VARIANTS = 4;

	private final int type;
	private final ProjectElement parent;
	private List<NavigationElement> childs;

	public StructureElement(int type, ProjectElement parent) {
		this.type = type;
		this.parent = parent;
	}

	public int getType() {
		return type;
	}

	public Project getProject() {
		if (parent != null)
			return parent.getProject();
		else
			return null;
	}

	@Override
	public List<NavigationElement> getChilds() {
		if (childs != null)
			return childs;
		childs = new ArrayList<>();
		if(type == PRODUCTION)
			addProducers(childs);
		if (type == CONSUMPTION)
			addConsumers(childs);
		if (type == VARIANTS)
			addVariants(childs);
		return childs;
	}

	private void addVariants(List<NavigationElement> childs) {
		Project p = getProject();
		if (p == null)
			return;
		for (Project v : p.getVariants())
			childs.add(new ProjectElement(this, v));
	}

	private void addConsumers(List<NavigationElement> childs) {
		Project p = getProject();
		if (p == null)
			return;
		for (Consumer c : p.getConsumers()) {
			childs.add(new FacilityElement(this, c));
		}
	}

	private void addProducers(List<NavigationElement> childs) {
		Project p = getProject();
		if (p == null)
			return;
		for(Producer producer : p.getProducers())
			childs.add(new FacilityElement(this, producer));
	}

	@Override
	public NavigationElement getParent() {
		return parent;
	}

	@Override
	public String getLabel() {
		switch (type) {
		case PRODUCTION:
			return M.HeatProduction;
		case DISTRIBUTION:
			return M.HeatDistribution;
		case CONSUMPTION:
			return M.HeatUsage;
		case COSTS:
			return M.Costs;
		case VARIANTS:
			return M.Variants;
		default:
			return M.Unknown;
		}
	}

	@Override
	public int compareTo(NavigationElement other) {
		if (!(other instanceof StructureElement))
			return 0;
		StructureElement otherElem = (StructureElement) other;
		return Integer.compare(this.type, otherElem.type);
	}

	@Override
	public Object getContent() {
		if (getProject() == null)
			return null;
		else
			return getProject().getId() + "_" + type;
	}

	@Override
	public void update() {
		childs = null;
	}

	@Override
	public Image getImage() {
		switch (type) {
		case CONSUMPTION:
			return Images.CONSUMER_16.img();
		case COSTS:
			return Images.COSTS_16.img();
		case PRODUCTION:
			return Images.PRODUCER_16.img();
		case DISTRIBUTION:
			return Images.PUMP_16.img();
		case VARIANTS:
			return Images.PROJECT_16.img();
		default:
			return null;
		}
	}

}
