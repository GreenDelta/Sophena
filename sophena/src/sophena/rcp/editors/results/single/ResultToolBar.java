package sophena.rcp.editors.results.single;

import java.io.File;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.part.EditorActionBarContributor;

import sophena.io.excel.ExcelExport;
import sophena.rcp.Icon;
import sophena.rcp.utils.Actions;
import sophena.rcp.utils.Editors;
import sophena.rcp.utils.FileChooser;
import sophena.rcp.utils.Popup;
import sophena.rcp.utils.Rcp;

public class ResultToolBar extends EditorActionBarContributor {

	@Override
	public void contributeToToolBar(IToolBarManager toolbar) {
		Action export = Actions.create("Nach Excel exportieren",
				Icon.EXCEL_16.des(), this::excelExport);
		toolbar.add(export);
	}

	private void excelExport() {
		ResultEditor editor = Editors.getActive();
		if (editor == null)
			return;
		File file = FileChooser.saveFile(editor.project.name + ".xlsx", "*.xlsx");
		if (file == null)
			return;
		ExcelExport export = new ExcelExport(editor.result, file);
		Rcp.run("Exportiere Ergebnisse ...", export, () -> {
			Popup.showInfo("Ergebnisse wurden exportiert");
		});
	}

}
