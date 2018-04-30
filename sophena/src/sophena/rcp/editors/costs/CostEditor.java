package sophena.rcp.editors.costs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.db.daos.ProjectDao;
import sophena.model.Product;
import sophena.model.ProductEntry;
import sophena.model.Project;
import sophena.model.descriptors.ProjectDescriptor;
import sophena.rcp.App;
import sophena.rcp.editors.Editor;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;
import sophena.rcp.utils.Strings;

public class CostEditor extends Editor {

	private Logger log = LoggerFactory.getLogger(getClass());

	private Project project;

	public static void open(ProjectDescriptor d) {
		if (d == null)
			return;
		Editors.closeIf(editor -> {
			if (!(editor instanceof CostEditor))
				return false;
			CostEditor e = (CostEditor) editor;
			return Strings.nullOrEqual(d.id, e.project.id);
		});
		EditorInput input = new EditorInput(
				d.id + "/costs", d.name);
		input.projectId = d.id;
		Editors.open(input, "sophena.CostEditor");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		EditorInput i = (EditorInput) input;
		ProjectDao dao = new ProjectDao(App.getDb());
		project = dao.get(i.projectId);
		setPartName(project.name + " - Investitionen");
	}

	public Project getProject() {
		return project;
	}

	@Override
	protected void addPages() {
		try {
			addPage(new OverviewPage(this));
		} catch (Exception e) {
			log.error("failed to add editor pages", e);
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			ProjectDao dao = new ProjectDao(App.getDb());
			Project dbProject = dao.get(project.id);
			// sync. with JPA managed entity; we could have the same instance
			// or two different instances here
			List<ProductEntry> entries = new ArrayList<>(
					project.productEntries);
			List<Product> ownProducts = new ArrayList<>(project.ownProducts);
			dbProject.productEntries.clear();
			dbProject.productEntries.addAll(entries);
			dbProject.ownProducts.clear();
			dbProject.ownProducts.addAll(ownProducts);
			project = dao.update(dbProject);
			setSaved();
		} catch (Exception e) {
			log.error("failed to save project cost settings");
		}
	}

	private static class EditorInput extends KeyEditorInput {

		private String projectId;

		public EditorInput(String key, String name) {
			super(key, name);
		}
	}
}
