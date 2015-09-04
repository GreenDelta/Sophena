package sophena.rcp.editors.sql;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.forms.editor.FormEditor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sophena.rcp.utils.Editors;
import sophena.rcp.utils.KeyEditorInput;

public class SqlEditor extends FormEditor {

	public static void open() {
		Editors.open(new KeyEditorInput("sql", "SQL"), "SqlEditor");
	}

	@Override
	protected void addPages() {
		try {
			addPage(new SqlEditorPage(this));
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(getClass());
			log.error("failed to open Sql Editor page", e);
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
