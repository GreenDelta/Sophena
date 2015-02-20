package sophena.rcp.editors.consumers;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sophena.model.Consumer;
import sophena.rcp.utils.Cache;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class ConsumerEditor extends FormEditor {

	private Logger log = LoggerFactory.getLogger(getClass());
	private Consumer consumer;

	public static void open(Consumer consumer) {
		if(consumer == null)
			return;
		String key = Cache.put(consumer);
		KeyEditorInput input = new KeyEditorInput(key, consumer.getName());
		Editors.open(input, "sophena.ConsumerEditor");
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		KeyEditorInput kIn = (KeyEditorInput)input;
		consumer = Cache.remove(kIn.getKey());
	}

	public Consumer getConsumer() {
		return consumer;
	}

	@Override
	protected void addPages() {
		try {
			addPage(new InfoPage(this));
		} catch (Exception e) {
			log.error("failed to add editor pages", e);
		}
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
}
