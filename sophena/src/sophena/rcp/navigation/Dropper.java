package sophena.rcp.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.navigator.CommonDropAdapter;
import org.eclipse.ui.navigator.CommonDropAdapterAssistant;

import sophena.db.daos.ProjectDao;
import sophena.model.Project;
import sophena.model.ProjectFolder;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;

public class Dropper extends CommonDropAdapterAssistant {

	@Override
	public IStatus validateDrop(Object target, int operation,
			TransferData data) {
		if ((target instanceof FolderElement)
				|| (target instanceof NavigationRoot))
			return Status.OK_STATUS;
		return null;
	}

	@Override
	public IStatus handleDrop(CommonDropAdapter adapter,
			DropTargetEvent event, Object target) {
		List<ProjectDescriptor> list = projects(event);
		if (list.isEmpty())
			return null;
		ProjectFolder folder = null;
		if (target instanceof FolderElement) {
			folder = ((FolderElement) target).content;
		}
		boolean updated = false;
		ProjectDao dao = new ProjectDao(App.getDb());
		for (ProjectDescriptor d : list) {
			Project p = dao.get(d.id);
			if (p == null)
				continue;
			if (Objects.equals(p.folder, folder))
				continue;
			p.folder = folder;
			dao.update(p);
			updated = true;
		}
		if (updated) {
			Navigator.refresh();
		}
		return Status.OK_STATUS;
	}

	private List<ProjectDescriptor> projects(DropTargetEvent e) {
		if (e == null)
			return Collections.emptyList();
		if (!(e.data instanceof IStructuredSelection))
			return Collections.emptyList();
		IStructuredSelection s = (IStructuredSelection) e.data;
		if (s.isEmpty())
			return Collections.emptyList();
		List<ProjectDescriptor> list = new ArrayList<>();
		for (Iterator<?> it = s.iterator(); it.hasNext();) {
			Object next = it.next();
			if (next instanceof ProjectElement) {
				list.add(((ProjectElement) next).content);
			}
		}
		return list;
	}

}
