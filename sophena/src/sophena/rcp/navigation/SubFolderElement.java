package sophena.rcp.navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.swt.graphics.Image;

import sophena.db.daos.ConsumerDao;
import sophena.db.daos.ProducerDao;
import sophena.db.daos.ProjectDao;
import sophena.model.ModelType;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.Icon;
import sophena.rcp.M;

public class SubFolderElement implements NavigationElement {

	private final SubFolderType type;
	private final ProjectElement parent;
	private List<NavigationElement> childs;

	public SubFolderElement(SubFolderType type, ProjectElement parent) {
		this.type = type;
		this.parent = parent;
	}

	public SubFolderType getType() {
		return type;
	}

	public ProjectDescriptor getProject() {
		if (parent != null)
			return parent.content;
		else
			return null;
	}

	@Override
	public List<NavigationElement> getChilds() {
		if (childs != null)
			return childs;
		childs = new ArrayList<>();
		update();
		return childs;
	}

	@Override
	public void update() {
		if (childs == null)
			return;
		switch (type) {
			case CONSUMPTION -> syncConsumers();
			case PRODUCTION -> {
				syncProducers();
				syncCleanings();
			}
			default -> {
			}
		}
	}

	private void syncConsumers() {
		ConsumerDao dao = new ConsumerDao(App.getDb());
		ChildSync.sync(childs,
				dao.getDescriptors(getProject()),
				ModelType.CONSUMER,
				d -> new ConsumerElement(this, d));
	}

	private void syncProducers() {
		ProducerDao dao = new ProducerDao(App.getDb());
		ChildSync.sync(childs,
				dao.getDescriptors(getProject()),
				ModelType.PRODUCER,
				d -> new ProducerElement(this, d));
	}

	private void syncCleanings() {
		ProjectDao dao = new ProjectDao(App.getDb());
		ChildSync.sync(childs,
				dao.getCleaningDescriptors(getProject()),
				ModelType.FLUE_GAS_CLEANING,
				d -> new CleaningElement(this, d));
	}

	@Override
	public NavigationElement getParent() {
		return parent;
	}

	@Override
	public String getLabel() {
		return switch (type) {
			case PRODUCTION -> M.HeatProduction;
			case DISTRIBUTION -> M.HeatDistribution;
			case CONSUMPTION -> M.HeatUsage;
			case COSTS -> "Investitionen";
			case RESULTS -> "Ergebnisse";
			default -> M.Unknown;
		};
	}

	@Override
	public int compareTo(NavigationElement obj) {
		if (!(obj instanceof SubFolderElement other))
			return 0;
		if (this.type == null || other.type == null)
			return 0;
		return this.type.ordinal() - other.type.ordinal();
	}

	@Override
	public Image getImage() {
		return switch (type) {
			case CONSUMPTION -> Icon.CONSUMER_16.img();
			case COSTS -> Icon.COSTS_16.img();
			case PRODUCTION -> Icon.PRODUCER_16.img();
			case DISTRIBUTION -> Icon.PUMP_16.img();
			case RESULTS -> Icon.LOAD_PROFILE_16.img();
			default -> null;
		};
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof SubFolderElement other))
			return false;
		return Objects.equals(this.getProject(), other.getProject())
				&& this.type == other.type;
	}

	@Override
	public int hashCode() {
		return Objects.hash(getProject(), getType());
	}
}
