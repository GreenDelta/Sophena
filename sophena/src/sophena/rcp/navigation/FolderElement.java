package sophena.rcp.navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.swt.graphics.Image;

import sophena.model.Consumer;
import sophena.model.Producer;
import sophena.model.Project;
import sophena.rcp.Images;
import sophena.rcp.M;

public class FolderElement implements NavigationElement {

	private final FolderType type;
	private final ProjectElement parent;
	private List<NavigationElement> childs;

	public FolderElement(FolderType type, ProjectElement parent) {
		this.type = type;
		this.parent = parent;
	}

	public FolderType getType() {
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
		if (type == FolderType.PRODUCTION)
			addProducers(childs);
		if (type == FolderType.CONSUMPTION)
			addConsumers(childs);
		if (type == FolderType.VARIANTS)
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
		for (Producer producer : p.getProducers())
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
		case ENERGY_RESULT:
			return "Ergebnis - Energie";
		case VARIANTS:
			return M.Variants;
		default:
			return M.Unknown;
		}
	}

	@Override
	public int compareTo(NavigationElement obj) {
		if (!(obj instanceof FolderElement))
			return 0;
		FolderElement other = (FolderElement) obj;
		if (this.type == null || other.type == null)
			return 0;
		return this.type.ordinal() - other.type.ordinal();
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
		case ENERGY_RESULT:
			return Images.LOAD_PROFILE_16.img();
		case VARIANTS:
			return Images.PROJECT_16.img();
		default:
			return null;
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof FolderElement))
			return false;
		FolderElement other = (FolderElement) obj;
		return Objects.equals(this.getProject(), other.getProject())
				&& Objects.equals(this.type, other.type);
	}

}
