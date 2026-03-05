package sophena.rcp.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.forms.editor.FormEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.rcp.utils.IEditor;

abstract public class Editor extends FormEditor implements IEditor {

	protected Logger log = LoggerFactory.getLogger(getClass());
	private boolean dirty;
	public final EventBus bus = new EventBus();

	@Override
	public void setDirty() {
		if (dirty)
			return;
		dirty = true;
		editorDirtyStateChanged();
	}

	public void setSaved() {
		if (!dirty)
			return;
		dirty = false;
		editorDirtyStateChanged();
	}

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	public void onClosed(Runnable fn) {
		if (fn == null)
			return;
		IWorkbenchSite site = getSite();
		if (site == null || site.getPage() == null)
			return;
		getSite().getPage().addPartListener(new IPartListener2() {
			@Override
			public void partClosed(IWorkbenchPartReference ref) {
				try {
					fn.run();
				} catch (Exception e) {
					log.error("failed to call after editor closed", e);
				}
			}
		});
	}
}
