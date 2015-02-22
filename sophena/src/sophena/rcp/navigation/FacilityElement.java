package sophena.rcp.navigation;

import java.util.Collections;
import java.util.List;
import org.eclipse.swt.graphics.Image;
import sophena.db.daos.Dao;
import sophena.model.Consumer;
import sophena.model.Facility;
import sophena.model.Project;
import sophena.rcp.App;
import sophena.rcp.Images;
import sophena.rcp.utils.Strings;

class FacilityElement implements NavigationElement {

	private StructureElement parent;
	private Facility facility;

	public FacilityElement(StructureElement parent, Facility facility) {
		this.parent = parent;
		this.facility = facility;
	}

	@Override
	public List<NavigationElement> getChilds() {
		return Collections.emptyList();
	}

	@Override
	public NavigationElement getParent() {
		return parent;
	}

	@Override
	public String getLabel() {
		return facility == null ? null : facility.getName();
	}

	@Override
	public int compareTo(NavigationElement other) {
		if (!(other instanceof FacilityElement))
			return 0;
		FacilityElement o = (FacilityElement) other;
		if (this.facility == null || o.facility == null)
			return 0;
		return Strings.compare(this.facility.getName(), o.facility.getName());
	}

	@Override
	public Object getContent() {
		return facility;
	}

	public Project getProject() {
		return parent == null ? null : parent.getProject();
	}

	@Override
	public void update() {
		if(facility instanceof Consumer) {
			Dao<Consumer> dao = new Dao<>(Consumer.class, App.getDb());
			facility = dao.get(facility.getId());
		}
	}

	@Override
	public Image getImage() {
		if (facility instanceof Consumer)
			return Images.CONSUMER_16.img();
		else
			return null;
	}
}
